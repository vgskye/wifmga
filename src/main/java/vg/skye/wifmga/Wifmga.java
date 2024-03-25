package vg.skye.wifmga;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.mojang.blaze3d.platform.GlConst.GL_CLAMP_TO_EDGE;
import static com.mojang.blaze3d.platform.GlConst.GL_LINEAR;
import static com.mojang.blaze3d.platform.GlConst.GL_NEAREST;
import static com.mojang.blaze3d.platform.GlConst.GL_TEXTURE_2D;
import static com.mojang.blaze3d.platform.GlConst.GL_TEXTURE_WRAP_S;
import static com.mojang.blaze3d.platform.GlConst.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glTexParameteri;

public class Wifmga implements ClientModInitializer {
	public static final String MOD_ID = "wifmga";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static WindowFramebuffer[] FRAMEBUFFERS = new WindowFramebuffer[0];
	private static ShaderProgram UP;
	private static ShaderProgram DOWN;

	private static void updateBuffers() {
		var display = MinecraftClient.getInstance().getFramebuffer();
		int screenWidth = display.textureWidth;
		int screenHeight = display.textureHeight;
		int maxIterations = MathHelper.ceilLog2(Math.min(screenWidth, screenHeight));
		int fbLength = Math.min(Config.INSTANCE.iterations, maxIterations);
		if (fbLength != FRAMEBUFFERS.length) {
			for (int i = fbLength; i < FRAMEBUFFERS.length; i++) {
				var buffer = FRAMEBUFFERS[i];
				if (buffer != null) {
					buffer.delete();
				}
			}
			FRAMEBUFFERS = Arrays.copyOf(FRAMEBUFFERS, fbLength);
		}
		for (int i = 0; i < FRAMEBUFFERS.length; i++) {
			var buffer = FRAMEBUFFERS[i];
			var width = screenWidth >> (i + 1);
			var height = screenHeight >> (i + 1);
			if (buffer != null) {
				buffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
			} else {
				FRAMEBUFFERS[i] = buffer = new WindowFramebuffer(width, height);
			}
			buffer.setTexFilter(GL_LINEAR);
			buffer.beginRead();
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			buffer.endRead();
		}
	}

	private static void maybeUpdateBuffers() {
		if (FRAMEBUFFERS.length == 0) {
			updateBuffers();
			return;
		}
		var display = MinecraftClient.getInstance().getFramebuffer();
		int screenWidth = display.textureWidth;
		int screenHeight = display.textureHeight;
		int maxIterations = MathHelper.ceilLog2(Math.min(screenWidth, screenHeight));
		int expectedLength = Math.min(Config.INSTANCE.iterations, maxIterations);
		var lastWidth = FRAMEBUFFERS[0].textureWidth;
		var lastHeight = FRAMEBUFFERS[0].textureHeight;
		if (expectedLength != FRAMEBUFFERS.length ||
				lastWidth != screenWidth >> 1 ||
				lastHeight != screenHeight >> 1) {
			updateBuffers();
		}
	}

	private static Identifier id(String name) {
		return new Identifier(MOD_ID, name);
	}

	@Override
	public void onInitializeClient() {
		LOGGER.info("Hello Fabric world!");

		CoreShaderRegistrationCallback.EVENT.register(ctx -> {
			ctx.register(
					id("up"),
					VertexFormats.POSITION_TEXTURE,
					shader -> UP = shader
			);
			ctx.register(
					id("down"),
					VertexFormats.POSITION_TEXTURE,
					shader -> DOWN = shader
			);
		});
	}

	public static void render() {
		maybeUpdateBuffers();
		var displayBuffer = MinecraftClient.getInstance().getFramebuffer();
		var scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
		double width = displayBuffer.viewportWidth / scaleFactor;
		double height = displayBuffer.viewportHeight / scaleFactor;

		// Switch to linear sampling
		displayBuffer.setTexFilter(GL_LINEAR);

		// Scale down
		for (int i = -1; i < FRAMEBUFFERS.length - 1; i++) {
			var bufferFrom = i == -1 ? displayBuffer : FRAMEBUFFERS[i];
			var bufferTo = FRAMEBUFFERS[i + 1];
			runShader(DOWN, bufferFrom, bufferTo, width, height);
		}

		// Scale up
		for (int i = (FRAMEBUFFERS.length - 1); i >= 0; i--) {
			var bufferFrom = FRAMEBUFFERS[i];
			var bufferTo = i == 0 ? displayBuffer : FRAMEBUFFERS[i - 1];
			runShader(UP, bufferFrom, bufferTo, width, height);
		}

		// Switch back to nearest
		displayBuffer.setTexFilter(GL_NEAREST);

		displayBuffer.beginWrite(true);
	}

	private static void runShader(ShaderProgram shader, Framebuffer from, Framebuffer to, double width, double height) {
		shader.addSampler("DiffuseSampler", from.getColorAttachment());
		shader.getUniform("InSize").set((float) from.textureWidth, from.textureHeight);
		shader.getUniform("Offset").set(Config.INSTANCE.offset);
		shader.bind();
		RenderSystem.setShader(() -> shader);

		from.endWrite();
		to.clear(MinecraftClient.IS_SYSTEM_MAC);
		to.beginWrite(false);

		var tessellator = Tessellator.getInstance();

		var bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(0, 0, 0).texture(0, 1).next();
		bufferBuilder.vertex(0, height, 0).texture(0, 0).next();
		bufferBuilder.vertex(width, height, 0).texture(1, 0).next();
		bufferBuilder.vertex(width, 0, 0).texture(1, 1).next();
		tessellator.draw();

		from.endRead();
	}
}