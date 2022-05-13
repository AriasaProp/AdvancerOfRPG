package com.ariasaproject.advancerofrpg.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import com.ariasaproject.advancerofrpg.files.FileHandle;

public class I18NBundle implements Disposable {
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final Locale ROOT_LOCALE = new Locale("", "", "");
	private static boolean simpleFormatter = false;
	private static boolean exceptionOnMissingKey = true;

	private I18NBundle parent;
	private Locale locale;
	private ObjectMap<String, String> properties;
	private TextFormatter formatter;

	public static boolean getSimpleFormatter() {
		return simpleFormatter;
	}

	public static void setSimpleFormatter(boolean enabled) {
		simpleFormatter = enabled;
	}

	public static boolean getExceptionOnMissingKey() {
		return exceptionOnMissingKey;
	}

	public static void setExceptionOnMissingKey(boolean enabled) {
		exceptionOnMissingKey = enabled;
	}

	/**
	 * Creates a new bundle using the specified <code>baseFileHandle</code>, the
	 * default locale and the default encoding "UTF-8".
	 *
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @return a bundle for the given base file handle and the default locale
	 * @throws NullPointerException     if <code>baseFileHandle</code> is
	 *                                  <code>null</code>
	 * @throws MissingResourceException if no bundle for the specified base file
	 *                                  handle can be found
	 */
	public static I18NBundle createBundle(FileHandle baseFileHandle) {
		return createBundleImpl(baseFileHandle, Locale.getDefault(), DEFAULT_ENCODING);
	}

	/**
	 * Creates a new bundle using the specified <code>baseFileHandle</code> and
	 * <code>locale</code>; the default encoding "UTF-8" is used.
	 *
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param locale         the locale for which a bundle is desired
	 * @return a bundle for the given base file handle and locale
	 * @throws NullPointerException     if <code>baseFileHandle</code> or
	 *                                  <code>locale</code> is <code>null</code>
	 * @throws MissingResourceException if no bundle for the specified base file
	 *                                  handle can be found
	 */
	public static I18NBundle createBundle(FileHandle baseFileHandle, Locale locale) {
		return createBundleImpl(baseFileHandle, locale, DEFAULT_ENCODING);
	}

	/**
	 * Creates a new bundle using the specified <code>baseFileHandle</code> and
	 * <code>encoding</code>; the default locale is used.
	 *
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param encoding       the charter encoding
	 * @return a bundle for the given base file handle and locale
	 * @throws NullPointerException     if <code>baseFileHandle</code> or
	 *                                  <code>encoding</code> is <code>null</code>
	 * @throws MissingResourceException if no bundle for the specified base file
	 *                                  handle can be found
	 */
	public static I18NBundle createBundle(FileHandle baseFileHandle, String encoding) {
		return createBundleImpl(baseFileHandle, Locale.getDefault(), encoding);
	}

	/**
	 * Creates a new bundle using the specified <code>baseFileHandle</code>,
	 * <code>locale</code> and <code>encoding</code>.
	 *
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param locale         the locale for which a bundle is desired
	 * @param encoding       the charter encoding
	 * @return a bundle for the given base file handle and locale
	 * @throws NullPointerException     if <code>baseFileHandle</code>,
	 *                                  <code>locale</code> or <code>encoding</code>
	 *                                  is <code>null</code>
	 * @throws MissingResourceException if no bundle for the specified base file
	 *                                  handle can be found
	 */
	public static I18NBundle createBundle(FileHandle baseFileHandle, Locale locale, String encoding) {
		return createBundleImpl(baseFileHandle, locale, encoding);
	}

	private static I18NBundle createBundleImpl(FileHandle baseFileHandle, Locale locale, String encoding) {
		if (baseFileHandle == null || locale == null || encoding == null)
			throw new NullPointerException();
		I18NBundle bundle = null;
		I18NBundle baseBundle = null;
		Locale targetLocale = locale;
		do {
			// Create the candidate locales
			List<Locale> candidateLocales = getCandidateLocales(targetLocale);
			// Load the bundle and its parents recursively
			bundle = loadBundleChain(baseFileHandle, encoding, candidateLocales, 0, baseBundle);
			// Check the loaded bundle (if any)
			if (bundle != null) {
				Locale bundleLocale = bundle.getLocale(); // WTH? GWT can't access bundle.locale directly
				boolean isBaseBundle = bundleLocale.equals(ROOT_LOCALE);
				if (!isBaseBundle || bundleLocale.equals(locale)) {
					// Found the bundle for the requested locale
					break;
				}
				if (candidateLocales.size() == 1 && bundleLocale.equals(candidateLocales.get(0))) {
					// Found the bundle for the only candidate locale
					break;
				}
				if (isBaseBundle && baseBundle == null) {
					// Store the base bundle and keep on processing the remaining fallback locales
					baseBundle = bundle;
				}
			}
			// Set next fallback locale
			targetLocale = getFallbackLocale(targetLocale);

		} while (targetLocale != null);
		if (bundle == null) {
			if (baseBundle == null) {
				// No bundle found
				throw new MissingResourceException(
						"Can't find bundle for base file handle " + baseFileHandle.path() + ", locale " + locale,
						baseFileHandle + "_" + locale, "");
			}
			// Set the base bundle to be returned
			bundle = baseBundle;
		}
		return bundle;
	}

	/**
	 * Returns a <code>List</code> of <code>Locale</code>s as candidate locales for
	 * the given <code>locale</code>. This method is called by the
	 * <code>createBundle</code> factory method each time the factory method tries
	 * finding a resource bundle for a target <code>Locale</code>.
	 * <p>
	 * <p>
	 * The sequence of the candidate locales also corresponds to the runtime
	 * resource lookup path (also known as the <I>parent chain</I>), if the
	 * corresponding resource bundles for the candidate locales exist and their
	 * parents are not defined by loaded resource bundles themselves. The last
	 * element of the list is always the {@linkplain Locale#ROOT root locale},
	 * meaning that the base bundle is the terminal of the parent chain.
	 * <p>
	 * <p>
	 * If the given locale is equal to <code>Locale.ROOT</code> (the root locale), a
	 * <code>List</code> containing only the root <code>Locale</code> is returned.
	 * In this case, the <code>createBundle</code> factory method loads only the
	 * base bundle as the resulting resource bundle.
	 * <p>
	 * <p>
	 * This implementation returns a <code>List</code> containing
	 * <code>Locale</code>s in the following sequence:
	 * <p>
	 * 
	 * <pre>
	 *     Locale(language, country, variant)
	 *     Locale(language, country)
	 *     Locale(language)
	 *     Locale.ROOT
	 * </pre>
	 * <p>
	 * where <code>language</code>, <code>country</code> and <code>variant</code>
	 * are the language, country and variant values of the given
	 * <code>locale</code>, respectively. Locales where the final component values
	 * are empty strings are omitted.
	 * <p>
	 * <p>
	 * For example, if the given base name is "Messages" and the given
	 * <code>locale</code> is <code>Locale("ja",&nbsp;"",&nbsp;"XX")</code>, then a
	 * <code>List</code> of <code>Locale</code>s:
	 * <p>
	 * 
	 * <pre>
	 *     Locale("ja", "", "XX")
	 *     Locale("ja")
	 *     Locale.ROOT
	 * </pre>
	 * <p>
	 * is returned. And if the resource bundles for the "ja" and ""
	 * <code>Locale</code>s are found, then the runtime resource lookup path (parent
	 * chain) is:
	 * <p>
	 * 
	 * <pre>
	 * Messages_ja -> Messages
	 * </pre>
	 *
	 * @param locale the locale for which a resource bundle is desired
	 * @return a <code>List</code> of candidate <code>Locale</code>s for the given
	 *         <code>locale</code>
	 * @throws NullPointerException if <code>locale</code> is <code>null</code>
	 */
	private static List<Locale> getCandidateLocales(Locale locale) {
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		List<Locale> locales = new ArrayList<Locale>(4);
		if (variant.length() > 0) {
			locales.add(locale);
		}
		if (country.length() > 0) {
			locales.add(locales.isEmpty() ? locale : new Locale(language, country));
		}
		if (language.length() > 0) {
			locales.add(locales.isEmpty() ? locale : new Locale(language));
		}
		locales.add(ROOT_LOCALE);
		return locales;
	}

	/**
	 * Returns a <code>Locale</code> to be used as a fallback locale for further
	 * bundle searches by the <code>createBundle</code> factory method. This method
	 * is called from the factory method every time when no resulting bundle has
	 * been found for <code>baseFileHandler</code> and <code>locale</code>, where
	 * locale is either the parameter for <code>createBundle</code> or the previous
	 * fallback locale returned by this method.
	 * <p>
	 * <p>
	 * This method returns the {@linkplain Locale#getDefault() default
	 * <code>Locale</code>} if the given <code>locale</code> isn't the default one.
	 * Otherwise, <code>null</code> is returned.
	 *
	 * @param locale the <code>Locale</code> for which <code>createBundle</code> has
	 *               been unable to find any resource bundles (except for the base
	 *               bundle)
	 * @return a <code>Locale</code> for the fallback search, or <code>null</code>
	 *         if no further fallback search is needed.
	 * @throws NullPointerException if <code>locale</code> is <code>null</code>
	 */
	private static Locale getFallbackLocale(Locale locale) {
		Locale defaultLocale = Locale.getDefault();
		return locale.equals(defaultLocale) ? null : defaultLocale;
	}

	private static I18NBundle loadBundleChain(FileHandle baseFileHandle, String encoding, List<Locale> candidateLocales,
			int candidateIndex, I18NBundle baseBundle) {
		Locale targetLocale = candidateLocales.get(candidateIndex);
		I18NBundle parent = null;
		if (candidateIndex != candidateLocales.size() - 1) {
			// Load recursively the parent having the next candidate locale
			parent = loadBundleChain(baseFileHandle, encoding, candidateLocales, candidateIndex + 1, baseBundle);
		} else if (baseBundle != null && targetLocale.equals(ROOT_LOCALE)) {
			return baseBundle;
		}
		// Load the bundle
		I18NBundle bundle = loadBundle(baseFileHandle, encoding, targetLocale);
		if (bundle != null) {
			bundle.parent = parent;
			return bundle;
		}
		return parent;
	}

	// Tries to load the bundle for the given locale.
	private static I18NBundle loadBundle(FileHandle baseFileHandle, String encoding, Locale targetLocale) {
		I18NBundle bundle = null;
		Reader reader = null;
		try {
			FileHandle fileHandle = toFileHandle(baseFileHandle, targetLocale);
			if (checkFileExistence(fileHandle)) {
				// Instantiate the bundle
				bundle = new I18NBundle();
				// Load bundle properties from the stream with the specified encoding
				reader = fileHandle.reader(encoding);
				bundle.load(reader);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (Throwable ignored) {
				}
		}
		if (bundle != null) {
			bundle.setLocale(targetLocale);
		}
		return bundle;
	}

	// On Android this is much faster than fh.exists(), see
	// https://github.com/libgdx/libgdx/issues/2342
	// Also this should fix a weird problem on iOS, see
	// https://github.com/libgdx/libgdx/issues/2345
	private static boolean checkFileExistence(FileHandle fh) {
		try {
			fh.read().close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Converts the given <code>baseFileHandle</code> and <code>locale</code> to the
	 * corresponding file handle.
	 * <p>
	 * <p>
	 * This implementation returns the <code>baseFileHandle</code>'s sibling with
	 * following value:
	 * <p>
	 * 
	 * <pre>
	 * baseFileHandle.name() + &quot;_&quot; + language + &quot;_&quot; + country + &quot;_&quot; + variant + &quot;.properties&quot;
	 * </pre>
	 * <p>
	 * where <code>language</code>, <code>country</code> and <code>variant</code>
	 * are the language, country and variant values of <code>locale</code>,
	 * respectively. Final component values that are empty Strings are omitted along
	 * with the preceding '_'. If all of the values are empty strings, then
	 * <code>baseFileHandle.name()</code> is returned with ".properties" appended.
	 *
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param locale         the locale for which a resource bundle should be loaded
	 * @return the file handle for the bundle
	 * @throws NullPointerException if <code>baseFileHandle</code> or
	 *                              <code>locale</code> is <code>null</code>
	 */
	private static FileHandle toFileHandle(FileHandle baseFileHandle, Locale locale) {
		StrBuilder sb = new StrBuilder(baseFileHandle.name());
		if (!locale.equals(ROOT_LOCALE)) {
			String language = locale.getLanguage();
			String country = locale.getCountry();
			String variant = locale.getVariant();
			boolean emptyLanguage = "".equals(language);
			boolean emptyCountry = "".equals(country);
			boolean emptyVariant = "".equals(variant);
			if (!(emptyLanguage && emptyCountry && emptyVariant)) {
				sb.append('_');
				if (!emptyVariant) {
					sb.append(language).append('_').append(country).append('_').append(variant);
				} else if (!emptyCountry) {
					sb.append(language).append('_').append(country);
				} else {
					sb.append(language);
				}
			}
		}
		return baseFileHandle.sibling(sb.append(".properties").toString());
	}

	/**
	 * Load the properties from the specified reader.
	 *
	 * @param reader the reader
	 * @throws IOException if an error occurred when reading from the input stream.
	 */
	// NOTE:
	// This method can't be private otherwise GWT can't access it from loadBundle()
	protected void load(Reader reader) throws IOException {
		properties = new ObjectMap<String, String>();
		PropertiesUtils.load(properties, reader);
	}

	/**
	 * Returns the locale of this bundle. This method can be used after a call to
	 * <code>createBundle()</code> to determine whether the resource bundle returned
	 * really corresponds to the requested locale or is a fallback.
	 *
	 * @return the locale of this bundle
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the bundle locale. This method is private because a bundle can't change
	 * the locale during its life.
	 *
	 * @param locale
	 */
	private void setLocale(Locale locale) {
		this.locale = locale;
		this.formatter = new TextFormatter(locale, !simpleFormatter);
	}

	/**
	 * Gets a string for the given key from this bundle or one of its parents.
	 *
	 * @param key the key for the desired string
	 * @return the string for the given key or the key surrounded by {@code ???} if
	 *         it cannot be found and {@link #getExceptionOnMissingKey()} returns
	 *         {@code false}
	 * @throws NullPointerException     if <code>key</code> is <code>null</code>
	 * @throws MissingResourceException if no string for the given key can be found
	 *                                  and {@link #getExceptionOnMissingKey()}
	 *                                  returns {@code true}
	 */
	public String get(String key) {
		String result = properties.get(key);
		if (result == null) {
			if (parent != null)
				result = parent.get(key);
			if (result == null) {
				if (exceptionOnMissingKey)
					throw new MissingResourceException("Can't find bundle key " + key, this.getClass().getName(), key);
				else
					return "???" + key + "???";
			}
		}
		return result;
	}

	/**
	 * Gets the string with the specified key from this bundle or one of its parent
	 * after replacing the given arguments if they occur.
	 *
	 * @param key  the key for the desired string
	 * @param args the arguments to be replaced in the string associated to the
	 *             given key.
	 * @return the string for the given key formatted with the given arguments
	 * @throws NullPointerException     if <code>key</code> is <code>null</code>
	 * @throws MissingResourceException if no string for the given key can be found
	 */
	public String format(String key, Object... args) {
		return formatter.format(get(key), args);
	}

	/**
	 * Sets the value of all localized strings to String placeholder so hardcoded,
	 * unlocalized values can be easily spotted. The I18NBundle won't be able to
	 * reset values after calling debug and should only be using during testing.
	 *
	 * @param placeholder
	 */
	public void debug(String placeholder) {
		ObjectMap.Keys<String> keys = properties.keys();
		if (keys == null)
			return;
		for (String s : keys) {
			properties.put(s, placeholder);
		}
	}

	@Override
	public void dispose() {
	}
}
