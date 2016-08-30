package ru.cheremin.scalarization.lab.misc;

import java.util.*;

import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.lab.Utils;
import ru.cheremin.scalarization.lab.Utils.BytecodePadder;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * Plain-old lambda (anonymous class as function).
 * <p/>
 * For now I see both 1.7 & 1.8 JVM successfully scalarize simple Summarizer for both
 * custom .forEach(), and THashSet.forEach()
 *
 * This happens if lambda code is not too long: with enough padding inlining could
 * be prevented, thus ruins chance for EA and scalarization. E.g. for 1.7 adding 34
 * PADDER.pad() statements in lambda is enough to stop scalarization.
 *
 * @author ruslan
 *         created 29/03/16 at 21:37
 */
public class POLambdaScenario extends AllocationScenario {
	public static final String BOX_TYPE_KEY = "scenario.box-type";
	public static final BoxType BOX_TYPE = BoxType.valueOf( System.getProperty( BOX_TYPE_KEY, BoxType.FOR_EACH_ARRAY.name() ) );


	private final String[] array = Utils.generateStringArray( SIZE );
	private final THashSet<String> set = new THashSet<>();

	private static final BytecodePadder PADDER = new BytecodePadder();

	{
		for( final String s : array ) {
			set.add( s );
		}
	}

	//TODO use more sophisticated lambda impl
	@Override
	public long run() {
		final SumLength lambda = new SumLength();
		BOX_TYPE.apply( this, new SumLength() );
		return lambda.sum + PADDER.value();
	}

	@Override
	public String additionalInfo() {
		return BOX_TYPE.name();
	}

	public static class SumLength implements TObjectProcedure<String> {
		private int sum = 0;

		@Override
		public boolean execute( final String s ) {

			PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();
			PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();
			PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();
			PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();
			PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();
			PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();
			PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();PADDER.pad();

			sum += s.length();
			return true;
		}
	}

	public static enum BoxType {
		FOR_EACH_ARRAY {
			@Override
			public void apply( final POLambdaScenario scenario,
			                   final TObjectProcedure<String> procedure ) {
				for( final String s : scenario.array ) {
					procedure.execute( s );
				}
			}
		},
		FOR_EACH_THASH_SET {
			@Override
			public void apply( final POLambdaScenario scenario,
			                   final TObjectProcedure<String> procedure ) {
				scenario.set.forEach( procedure );
			}
		};

		public abstract void apply( final POLambdaScenario scenario,
		                            final TObjectProcedure<String> procedure );
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, /*0, 1, 2, 4, */64, 65 ),
				allOf( BOX_TYPE_KEY, BoxType.FOR_EACH_THASH_SET )
		);
	}
}
