package type_check;

import ast.Program;
import ast.Type;
import utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassDesc {
    Map<String, ClassEnv> classes;

    private ClassDesc(List<ClassEnv> classes) {
        this.classes = classes.stream()
                .collect(Collectors.toMap(
                        classEnv -> classEnv.name.name,
                        classEnv -> classEnv));
    }

    public List<Type> getAllClassTypes() {
        return classes.values().stream()
                .map(classEnv -> classEnv.name)
                .collect(Collectors.toList());
    }

    public Optional<ClassEnv> getClassEnv(Type cls) {
        return classes.containsKey(cls.name) ? Optional.of(classes.get(cls.name)) : Optional.empty();
    }

    public static ClassDesc initialize(Program program) throws CompileErrorExp {
        var errors = new ArrayList<CompileError>();
        if (ListUtils.isNotUnique(program.classDecls, classDecl -> classDecl.name.name))
            errors.add(new CompileError("Duplicated class names", program));
        var classes = new ArrayList<ClassEnv>();
        for (var cls : program.classDecls) {
            try {
                classes.add(ClassEnv.initialize(cls));
            } catch (CompileErrorExp e) {
                errors.addAll(e.errors);
            }
        }
        if (!errors.isEmpty())
            throw new CompileErrorExp(errors);
        return new ClassDesc(classes);
    }
}
