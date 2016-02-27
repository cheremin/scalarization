package ru.cheremin.scalarization.infra;

import java.lang.annotation.*;

/**
 * To mark "public static List[ScenarioRun]" methods supplying scenario run
 * parameters
 *
 * @author ruslan
 *         created 27/02/16 at 17:37
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
public @interface ScenarioRunArgs {
}
