package mg.itu.prom16.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Validation {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER, ElementType.FIELD })
    public @interface NotNull {
        String message() default "Le champ ne peut pas être null.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER, ElementType.FIELD })
    public @interface Valid {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER, ElementType.FIELD })
    public @interface Size {
        int min() default 0;

        int max() default Integer.MAX_VALUE;

        String message() default "La taille du champ doit être compris entre les valeurs min et max.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER, ElementType.FIELD })
    public @interface Min {
        long value();

        String message() default "La valeur doit être supérieure ou égale à la valeur min.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER, ElementType.FIELD })
    public @interface Max {
        long value();

        String message() default "La valeur ne peut pas dépasser de la valeur max.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER, ElementType.FIELD })
    public @interface Email {
        String message() default "L'adresse email doit être valide.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER, ElementType.FIELD })
    public @interface Pattern {
        String regexp();

        String message() default "Le format du champ ne correspond pas à l'expression régulière.";
    }
}
