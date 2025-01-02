// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.irisshaders.iris.gl.shader;

import org.lwjgl.opengl.*;

/**
 * An enumeration over the supported OpenGL shader types.
 */
public enum ShaderType {
	VERTEX(GL20.GL_VERTEX_SHADER),
	GEOMETRY(GL32.GL_GEOMETRY_SHADER),
	FRAGMENT(GL20.GL_FRAGMENT_SHADER),
	COMPUTE(GL43.GL_COMPUTE_SHADER),
	TESSELATION_CONTROL(GL40.GL_TESS_CONTROL_SHADER),
	TESSELATION_EVAL(GL40.GL_TESS_EVALUATION_SHADER);

	public final int id;

	ShaderType(int id) {
		this.id = id;
	}
}
