package reflection;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Arrays;
import java.util.Locale;

public class Answer {

    private String classImpl;
    private AnswerFileObject fileObject;
    private String methodName;

    private final String className = "TestClass";

    public Answer(String signature, String a) throws AnswerCompilationException {
        classImpl = "public class " + className + " { " + signature + " { " + a + " } }";
        System.out.println(classImpl);
        try {
            fileObject = new AnswerFileObject(new URI(className + ".java"), JavaFileObject.Kind.SOURCE, classImpl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        compileClass();
        methodName = signature.strip().split("[, )(]+")[2];
    }

    // source: java2s.com/Code/Java/JDK-6/CompileaJavafilewithJavaCompiler.htm
    private void compileClass() throws AnswerCompilationException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(fileObject);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
                null, compilationUnits);
        boolean success = task.call();
        try {
            fileManager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Successful compilation: " + success);
        if (!success) {
            String message = "";
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                message += diagnostic.getMessage(Locale.ENGLISH);
            }

            throw new AnswerCompilationException(message); // TODO: put an error message or something
        }
    }

    public Object execute(TypeValuePair param) {
        try {
            System.out.println(className);
            System.out.println(methodName);
            File f = new File(".");
            ClassLoader cl = new URLClassLoader(new URL[]{f.toURI().toURL()});
            Class<?> c = cl.loadClass(className);
            System.out.println(param.getJavaType().getType());
            System.out.println(param.getJavaValue().getClass());
            Method method = c.getMethod(methodName, param.getJavaType().getType());

            return method.invoke(c.getConstructor().newInstance(), param.getJavaValue());
        } catch (MalformedURLException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

    }

}
