package type_check;

import ast.Identifier;
import ast.Type;
import utils.PersistentMap;

import java.util.Optional;

public class TypeEnv {
    private PersistentMap<String, Type> map;

    private TypeEnv(PersistentMap<String, Type> map) {
        this.map = map;
    }

    public static TypeEnv emptyEnv() {
        return new TypeEnv(PersistentMap.empty());
    }

    TypeEnv extend(Identifier id, Type type) {
        return new TypeEnv(map.add(id.name, type));
    }

    Optional<Type> getType(Identifier id) {
        return map.lookup(id.name);
    }
}
