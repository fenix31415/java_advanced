package ru.ifmo.rain.klepov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.BiFunction;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Implementation class for JarImpler interface.
 */
public class Implementor implements JarImpler {
    // :NOTE: Это были не константы
    /**
     * String that equals to default indentation.
     */
    private static final String TAB_STRING = "    ";
    /**
     * Double indentation. Equals {@link #TAB_STRING} {@code +} {@link #TAB_STRING}.
     */
    private static final String DOUBLE_TAB = TAB_STRING + TAB_STRING;

    /**
     * Returns default value for the Class instance.
     * @param token Class instance.
     * @return Default value for Class instance.
     */
    private static String getDefaultValue(final Class<?> token) {
		if (!token.isPrimitive())
			return "null";
		return token == boolean.class ? "false" : "0";
    }

    // :NOTE: Типы и так видны в Javadoc
	/**
	 * Converts given string to unicode escaping
	 * @param in string to convert
	 * @return converted string
	 */
	private static String toUnicode(final String in) {
		final StringBuilder b = new StringBuilder();
		for (final char c : in.toCharArray()) {
			if (c >= 128) {
				b.append(String.format("\\u%04x", (int) c));
			} else {
				b.append(c);
			}
		}
		return b.toString();
	}

	/**
	 * Write args one by one
	 * @param writer Output source
	 * @param args A variadic set of Objects to print
	 * @throws IOException If I/O error occurs while writing
	 */
	private static void append(final Writer writer, final Object... args) throws IOException {
		for (final Object arg : args) {
			writer.write(toUnicode(arg.toString()));
		}
	}
	
	/**
	 * NewLine-version of {@link #append(Writer, Object...)}
	 * @param writer Output source
	 * @param args A variadic set of Objects to print
	 * @throws IOException If I/O error occurs while writing
	 */
	private static void appendln(final Writer writer, final Object... args) throws IOException {
		append(writer, args);
		writer.write(System.lineSeparator());
	}

    /**
     * Write method's implementation to writer.
     * @param m Implementing Method
     * @param w Output source.
     * @throws IOException If I/O error occurs while writing.
     */
    private static void printMethod(final Method m, final Writer w) throws IOException {
        append(w, TAB_STRING);
        final int modifiers = m.getModifiers();
        if (Modifier.isProtected(modifiers)) {
            append(w, "protected");
        } else if (Modifier.isPublic(modifiers)) {
            append(w, "public");
        }
        append(w, String.format(" %s %s(", m.getReturnType().getCanonicalName(), m.getName()));
        printArray(w, m.getParameterTypes(), (t, i) -> String.format("%s a%d", t.getCanonicalName(), i));
        appendln(w, ") {");
        append(w, String.format("%sreturn", DOUBLE_TAB));
        if (m.getReturnType() != void.class) {
            append(w, String.format(" %s", getDefaultValue(m.getReturnType())));
        }
        appendln(w, ";");
        appendln(w, String.format("%s}", TAB_STRING));
    }


    /**
     * Write constructor implementation to writer.
     * @param c Implementing Constructor
     * @param w Output source.
     * @param nameImpl Name of resulting class.
     * @throws IOException If I/O error occurs while writing.
     */
    private static void printConstructor(final Constructor<?> c, final Writer w, final String nameImpl) throws IOException {
        final Class<?>[] args = c.getParameterTypes();
        final Class<?>[] exceptions = c.getExceptionTypes();
        if (args.length == 0 && exceptions.length == 0) {
            return;
        }
		
        append(w, String.format("%spublic %s()", TAB_STRING, nameImpl));
        if (exceptions.length > 0) {
            append(w, " throws ");
            printArray(w, exceptions, (t, i) -> t.getCanonicalName());
        }
        appendln(w, " {");
        append(w, String.format("%ssuper(", DOUBLE_TAB));
        printArray(w, args, (t, i) -> String.format("(%s)%s", t.getCanonicalName(), getDefaultValue(t)));
        appendln(w, ");");
        appendln(w, String.format("%s}", TAB_STRING));
    }
	
    /**
     * Writes mapped arguments to writer separated by comma.
     * @param w Output source.
     * @param a Array of arguments.
     * @param mapFunction Function formatting printing elements.
     * @param <T> Type of arguments.
     * @throws IOException If I/O error occurs while writing.
     */
    private static <T> void printArray(final Writer w, final T[] a, final BiFunction<T, Integer, String> mapFunction) throws IOException {
        // :NOTE: Ааа!
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                append(w, ", ");
            }
            append(w, mapFunction.apply(a[i], i));
        }
    }
	
	/**
	 * Adds only requiring methods from given.
	 * @param set A set of requiring methods.
	 * @param methods An array of candidate methods.
	 */
	private static void getMethods(Set<Signature> set, Method[] methods) {
		Arrays.stream(methods)
				.map(Signature::new)
				.forEach(set::add);
    }
	
	/**
	 * Adds all requiring methods.
	 * @param token Implementing Class-token.
	 * @return A List of collected methods.
	 */
	private static List<Method> collectMethods(final Class<?> token) {
		Set<Signature> methods = new HashSet<>();
		getMethods(methods, token.getMethods());
		for (Class<?> clazz = token; clazz != null; clazz = clazz.getSuperclass()) {
			getMethods(methods, clazz.getDeclaredMethods());
		}

		List<Method> result = methods.stream()
				.map(Signature::getMethod)
				.filter(m -> Modifier.isAbstract(m.getModifiers()))
				.collect(Collectors.toList());
		return result;
	}

    /**
     * Write methods and constructors implementing token to writer.
     * @param token Implementing Class-token.
     * @param w Output source.
     * @param nameImpl Name of resulting class.
     * @throws IOException If I/O error occurs while writing.
     * @throws ImplerException If impossible to implement token.
     */
    private static void printBody(final Class<?> token, final Writer w, final String nameImpl) throws IOException, ImplerException {
        // :NOTE: Создайте внутри и верните
		final List<Method> methods = collectMethods(token);

        // :NOTE: Элегантнее было бы использовать стримы
		if (!token.isInterface()) {
			printConstructor(Arrays.stream(token.getDeclaredConstructors())
				.filter(c -> !Modifier.isPrivate(c.getModifiers())).findAny()
				.orElseThrow(() -> new ImplerException("All constructors in given class are private.")),
				w, nameImpl);
        }
        appendln(w);
		for (Method t : methods) {
			printMethod(t, w);
		}
        /*for (final var pair : methods.entrySet()) {
            printMethod(pair.getKey().getMethod(), w);
        }*/
		
    }

    /**
     * Write token implementation
     * @param token Implementing Class token.
     * @param w Output source.
     * @param nameImpl Name of resulting class.
     * @throws IOException If I/O error occurs while writing.
     * @throws ImplerException If impossible to implement token.
     */
    private static void printImpl(final Class<?> token, final Writer w, final String nameImpl) throws IOException, ImplerException {
        final String packageName = token.getPackageName();
        if (!packageName.isEmpty()) {
            // :NOTE: Логично было бы сделать append и appendln
            appendln(w, String.format("package %s;", token.getPackageName()));
            appendln(w);
        }
        appendln(w, String.format("public class %s %s %s {", nameImpl, token.isInterface() ? "implements" : "extends",
                token.getCanonicalName()));
        printBody(token, w, nameImpl);
        appendln(w, "}");
    }

    /**
     * Implementation of FileVisitor, that recursively removes temporary directories.
     */
    private final static FileVisitor<Path> CLEANER = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes basicFileAttributes) throws IOException {
            Files.delete(path);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path directory, final IOException ioException) throws IOException {
            Files.delete(directory);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Write implementation of token in root path.
     * @param token Class-token to create inplementation for.
     * @param root root directory.
     * @return Subpath from root to .java file.
     * @throws ImplerException If impossible to implement token or an I/O error occurs.
     */
    private static Path staticImplement(final Class<?> token, final Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Input must not be null.");
        }
        if (token.isArray() || token.isPrimitive() || Modifier.isFinal(token.getModifiers()) ||
                Enum.class.isAssignableFrom(token) || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Can not extend/implement token");
        }
		
        final String nameImpl = token.getSimpleName() + "Impl";
        Path packagePath = Path.of("");
        for (final String s : token.getPackageName().split("\\.")) {
            packagePath = packagePath.resolve(s);
        }
        final Path filePath = root.resolve(packagePath).resolve(nameImpl + ".java");
		
        try (final Writer writer = Files.newBufferedWriter(createFile(filePath))) {
            try {
                printImpl(token, writer, nameImpl);
            } catch (final IOException e) {
                throw new ImplerException("Cannot write result", e);
            }
        } catch (final IOException e) {
            throw new ImplerException("Cannot close out file", e);
        }
        return packagePath;
    }

    /**
     * Recursive delete of folder.
     * @param path represents the folder to recursively delete.
     * @throws IOException In case {@link SimpleFileVisitor} fails to delete files
     */
    private static void clean(final Path path) throws IOException {
        Files.walkFileTree(path, CLEANER);
    }

    /**
     * Create implementation of token and pack it to jar.
     * @param token Class-token, that have to be implemented.
     * @param jarFile Path to result jar file.
     * @throws ImplerException If impossible to implement token or if an I/O error occurs.
     */
    private static void staticImplementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        final Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "temp");
        } catch (final IOException e) {
            throw new ImplerException("Can not create temp directory", e);
        }
		
        try {
            final Path classPath;
            try {
                classPath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (final URISyntaxException e) {
                throw new ImplerException("Cannot get class path", e);
            }
			
            final Path packagePath = staticImplement(token, tmpDir);
            final String nameImpl = token.getSimpleName() + "Impl";
            final Path javaFilePath = tmpDir.resolve(packagePath).resolve(nameImpl + ".java");
			
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            final String[] args = new String[]{
                    "-cp",
                    classPath.toString(),
                    "-encoding",
                    "utf8",
                    javaFilePath.toString()
            };
            if (compiler == null || compiler.run(null, null, null, args) != 0) {
                throw new ImplerException("Can not compile files");
            }

            final Manifest manifest = new Manifest();
            final Attributes attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
			
            try (final JarOutputStream writer = new JarOutputStream(Files.newOutputStream(createFile(jarFile)), manifest)) {
                // :NOTE: Раздлители, в теории, бывают не тоько \ и /
                writer.putNextEntry(new ZipEntry(packagePath.resolve(nameImpl + ".class").toString()
                        .replace(File.separatorChar, '/')));
                Files.copy(tmpDir.resolve(packagePath).resolve(nameImpl + ".class"), writer);
            } catch (final IOException e) {
                throw new ImplerException("Can not write to JAR file", e);
            }
        } finally {
            try {
				clean(tmpDir);
            } catch (final IOException e) {
                throw new ImplerException("Can not remove tmp directory", e);
            }
        }
    }

    /**
     * Create file and parent directories.
     * @param path Path instance, representing path to file.
     * @return Path to this file.
     * @throws ImplerException If cannot create file or directories.
     */
    private static Path createFile(final Path path) throws ImplerException {
        final Path parentPath = path.getParent();
        if (parentPath != null) {
            try {
                Files.createDirectories(parentPath);
            } catch (final IOException e) {
                throw new ImplerException("Cannot create directories", e);
            }
        }
        return path;
    }

    /**
     * Create implementation of token.
     * @param token Class-token to create implementation for.
     * @param root Root directory.
     * @throws ImplerException If impossible to implement token or if an I/O error occurs.
     */
    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        staticImplement(token, root);
    }

    /**
     * Create implementation of token and pack it to jar.
     * @param token Class-token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException If impossible to implement token or if an I/O error occurs.
     */
    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        staticImplementJar(token, jarFile);
    }

	/**
	 * Determines if the arguments are correct.
	 * @param args arguments of a program.
	 * @return true whenever args is a correct set of arguments.
	 */
	private static boolean checkArgs(final String[] args) {
		return (args != null) && (args.length == 2 || (args.length == 3 && args[0].equals("-jar")));
	}

    // :NOTE: Использование <code>
    // :NOTE: Ссылки на самого себя
    /**
     * Used to choose jar-mode or simple mode of execution.
     * Has two possuble options, depending on arguments: <br>
     *  2 arguments: {@code className rootPath} - executes {@link #implement(Class, Path)} with arguments 0, 1<br>
     *  3 arguments: {@code -jar className jarPath} - executes {@link #implementJar(Class, Path)} with arguments 1, 2<br>
     *
     * @param args mode, if present, and remaining: filename and target filename
     */
    public static void main(final String[] args) {
		if (!checkArgs(args)) {
			System.out.println("Usage: <class_name> <path> or -jar <class_name> <path>");
			return;
		}
		
		final Class<?> token;
		final int filenameInd = (args[0].equals("-jar") ? 1 : 0);
		final int filepathInd = filenameInd + 1;

		try {
			token = Class.forName(args[filenameInd], false, Implementor.class.getClassLoader());
		} catch (final ClassNotFoundException e) {
			System.out.println("Cannot to load given class " + e.getMessage());
			e.printStackTrace(System.out);
			return;
		}
		
		try {
			if (args.length == 2) {
				staticImplement(token, Path.of(args[filepathInd]));
			} else {
				staticImplementJar(token, Path.of(args[filepathInd]));
			}
		} catch (final ImplerException e) {
			System.out.println(e.getMessage());
			if (e.getCause() != null) {
				e.getCause().printStackTrace(System.out);
			}
		} catch (final InvalidPathException e) {
			System.out.println("Invalid path to file");
			System.out.println(e.getMessage());
		}
    }
	
	/**
	 * Class stored {@link Method} and make it hashable and so insertable to {@link HashSet}
	 */
	private static class Signature {
		/**
		 * Actual function name
		 */
		private final String name;
		/**
		 *  Argument types
		 */
		private final Class<?>[] args;

		/**
		 * Instance of Method class
		 */
		private final Method method;

		/**
		 * Default constructor, accepts Method
		 * @param method Method to assign
		 */
		public Signature(final Method method) {
			this.method = method;
			this.name = method.getName();
			this.args = method.getParameterTypes();
		}

		/**
		 * Default getter
		 * @return Method value, stored in the Signature
		 */
		public Method getMethod() {
			return method;
		}

		/**
		 * Implementation of {@link Object#equals}
		 * @param o object Object to compare with
		 * @return true if objects are equal, false otherwise
		 */
		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final Signature signature = (Signature) o;
			if (!Arrays.equals(args, signature.args)) return false;
			return name.equals(signature.name);
		}

		/**
		 * Implementation of {@link Object#hashCode}
		 * @return A hashcode calculated for the instance
		 */
		@Override
		public int hashCode() {
			int result = Arrays.hashCode(args);
			result = 31 * result + name.hashCode();
			return result;
		}
	}
}
