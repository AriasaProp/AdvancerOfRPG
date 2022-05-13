package com.ariasaproject.advancerofrpg.assets.loaders;

import java.util.Locale;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.I18NBundle;

public class I18NBundleLoader extends AsynchronousAssetLoader<I18NBundle, I18NBundleLoader.I18NBundleParameter> {

	I18NBundle bundle;

	public I18NBundleLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetContainer manager, String fileName, FileHandle file, I18NBundleParameter parameter) {
		this.bundle = null;
		Locale locale;
		String encoding;
		if (parameter == null) {
			locale = Locale.getDefault();
			encoding = null;
		} else {
			locale = parameter.locale == null ? Locale.getDefault() : parameter.locale;
			encoding = parameter.encoding;
		}
		if (encoding == null) {
			this.bundle = I18NBundle.createBundle(file, locale);
		} else {
			this.bundle = I18NBundle.createBundle(file, locale, encoding);
		}
	}

	@Override
	public I18NBundle loadSync(AssetContainer manager, String fileName, FileHandle file, I18NBundleParameter parameter) {
		I18NBundle bundle = this.bundle;
		this.bundle = null;
		return bundle;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, I18NBundleParameter parameter) {
		return null;
	}

	static public class I18NBundleParameter extends AssetLoaderParameters<I18NBundle> {
		public final Locale locale;
		public final String encoding;

		public I18NBundleParameter() {
			this(null, null);
		}

		public I18NBundleParameter(Locale locale) {
			this(locale, null);
		}

		public I18NBundleParameter(Locale locale, String encoding) {
			this.locale = locale;
			this.encoding = encoding;
		}
	}

}
