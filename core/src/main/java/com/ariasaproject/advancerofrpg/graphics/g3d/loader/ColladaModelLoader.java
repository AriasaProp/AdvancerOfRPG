package com.ariasaproject.advancerofrpg.graphics.g3d.loader;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.assets.loaders.FileHandleResolver;
import com.ariasaproject.advancerofrpg.assets.loaders.ModelLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.ModelLoader.ModelParameters;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.data.ModelData;
import com.ariasaproject.advancerofrpg.utils.ObjectMap;
import com.ariasaproject.advancerofrpg.utils.XmlReader;
import com.ariasaproject.advancerofrpg.utils.XmlReader.Element;

public class ColladaModelLoader extends ModelLoader<ModelParameters> {

	public ColladaModelLoader() {
		this(null);
	}

	public ColladaModelLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public ModelData loadModelData(FileHandle fileHandle, ModelLoader.ModelParameters parameters) {
		Element e = XmlReader.parse(fileHandle);
		StringBuilder sb = new StringBuilder();
		sb.append("begin");
		innerChild(sb, e, 0);
		sb.append("\nend");
		FileHandle fh = GraphFunc.app.getFiles().external("log.txt");
		fh.mkdirs();
		fh.writeString(sb.toString(), false);
		GraphFunc.app.log("ColladaLoader", fh.path());

		return null;
	}

	static void innerChild(StringBuilder sb, Element e, int lvl) {
		String l = "\n";
		for (int i = 0; i < lvl; i++)
			l += ">";
		sb.append(l).append("#" + e.getName() + ":" + (e.getText() == null ? "" : e.getText()));
		if (e.getAttributes() != null)
			for (ObjectMap.Entry<String, String> q : e.getAttributes()) {
				sb.append(l).append(" <" + q.key + "> = " + q.value);
			}
		for (int i = 0; i < e.getChildCount(); i++) {
			innerChild(sb, e.getChild(i), lvl + 1);
		}

	}
}
