package ru.cheremin.scalarization.scenarios.tricky;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.JvmArg.JvmExtendedProperty;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static java.util.Arrays.asList;
import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * Investigate scalarization of really big objects, up to [64 x long] = 256 bytes.
 * Idea is to enumerate scalarization limits we're breaching with object size increased.
 * One of the limits assumed was TrackedInitializationLimit (=50) which should limit
 * size of scalarized object in HeapWords(=8 bytes). So I expect big objects to stop
 * scalarizing at 51*long fields.
 *
 * In practice, scalarization stops at ~40 long fields, because constructor just
 * becomes too big to be inlined. By updating FreqInlineSize from 325 to 1000, objects
 * up to 64 long fields scalarized successfully.
 *
 * TODO RC: The question remains: why TrackedInitializationLimit is ignored?
 *
 * @author ruslan
 *         created 01/04/16 at 00:12
 */
public class BigObjectsAllocationSCenario extends AllocationScenario {

	private final ThreadLocalRandom rnd = ThreadLocalRandom.current();

	@Override
	public long run() {
		switch( SIZE ) {
			case 1: {
				final Tuple1 tuple = new Tuple1( rnd.nextLong() );
				return tuple.value1;
			}
			case 2: {
				final Tuple2 tuple = new Tuple2( rnd.nextLong(), rnd.nextLong() );
				return tuple.value1;
			}
			case 32: {
				final Tuple32 tuple = new Tuple32( rnd );
				return tuple.value31;
			}
			case 44: {
				final Tuple44 tuple = new Tuple44( rnd );
				return tuple.value31;
			}
			case 50: {
				final Tuple50 tuple = new Tuple50( rnd );
				return tuple.value40;
			}
			case 51: {
				final Tuple51 tuple = new Tuple51( rnd );
				return tuple.value15;
			}
			case 64: {
				final Tuple64 tuple = new Tuple64( rnd );
				return tuple.value64;
			}
			default: {
				throw new IllegalStateException( "SIZE=" + SIZE + " is not supported" );
			}
		}

	}


	public static class Tuple1 {
		public long value1;

		public Tuple1( final long value1 ) {
			this.value1 = value1;
		}
	}

	public static class Tuple2 {
		public long value1;
		public long value2;

		public Tuple2( final long value1, final long value2 ) {
			this.value1 = value1;
			this.value2 = value2;
		}
	}

	public static class Tuple32 {
		public long value01, value11, value21, value31;
		public long value02, value12, value22, value32;
		public long value03, value13, value23;
		public long value04, value14, value24;
		public long value05, value15, value25;
		public long value06, value16, value26;
		public long value07, value17, value27;
		public long value08, value18, value28;
		public long value09, value19, value29;
		public long value10, value20, value30;

		public Tuple32( final Random rnd ) {
			this.value01 = rnd.nextLong();
			this.value11 = rnd.nextLong();
			this.value21 = rnd.nextLong();
			this.value31 = rnd.nextLong();
			this.value02 = rnd.nextLong();
			this.value12 = rnd.nextLong();
			this.value22 = rnd.nextLong();
			this.value32 = rnd.nextLong();
			this.value03 = rnd.nextLong();
			this.value13 = rnd.nextLong();
			this.value23 = rnd.nextLong();
			this.value04 = rnd.nextLong();
			this.value14 = rnd.nextLong();
			this.value24 = rnd.nextLong();
			this.value05 = rnd.nextLong();
			this.value15 = rnd.nextLong();
			this.value25 = rnd.nextLong();
			this.value06 = rnd.nextLong();
			this.value16 = rnd.nextLong();
			this.value26 = rnd.nextLong();
			this.value07 = rnd.nextLong();
			this.value17 = rnd.nextLong();
			this.value27 = rnd.nextLong();
			this.value08 = rnd.nextLong();
			this.value18 = rnd.nextLong();
			this.value28 = rnd.nextLong();
			this.value09 = rnd.nextLong();
			this.value19 = rnd.nextLong();
			this.value29 = rnd.nextLong();
			this.value10 = rnd.nextLong();
			this.value20 = rnd.nextLong();
			this.value30 = rnd.nextLong();
		}

	}

	public static class Tuple44 {
		public long value01, value11, value21, value31, value41;
		public long value02, value12, value22, value32, value42;
		public long value03, value13, value23, value33, value43;
		public long value04, value14, value24, value34, value44;
		public long value05, value15, value25, value35;
		public long value06, value16, value26, value36;
		public long value07, value17, value27, value37;
		public long value08, value18, value28, value38;
		public long value09, value19, value29, value39;
		public long value10, value20, value30, value40;

		public Tuple44( final Random rnd ) {
			this.value01 = rnd.nextLong();
			this.value11 = rnd.nextLong();
			this.value21 = rnd.nextLong();
			this.value31 = rnd.nextLong();
			this.value41 = rnd.nextLong();
			this.value02 = rnd.nextLong();
			this.value12 = rnd.nextLong();
			this.value22 = rnd.nextLong();
			this.value32 = rnd.nextLong();
			this.value42 = rnd.nextLong();
			this.value03 = rnd.nextLong();
			this.value13 = rnd.nextLong();
			this.value23 = rnd.nextLong();
			this.value33 = rnd.nextLong();
			this.value43 = rnd.nextLong();
			this.value04 = rnd.nextLong();
			this.value14 = rnd.nextLong();
			this.value24 = rnd.nextLong();
			this.value34 = rnd.nextLong();
			this.value44 = rnd.nextLong();
			this.value05 = rnd.nextLong();
			this.value15 = rnd.nextLong();
			this.value25 = rnd.nextLong();
			this.value35 = rnd.nextLong();
			this.value06 = rnd.nextLong();
			this.value16 = rnd.nextLong();
			this.value26 = rnd.nextLong();
			this.value36 = rnd.nextLong();
			this.value07 = rnd.nextLong();
			this.value17 = rnd.nextLong();
			this.value27 = rnd.nextLong();
			this.value37 = rnd.nextLong();
			this.value08 = rnd.nextLong();
			this.value18 = rnd.nextLong();
			this.value28 = rnd.nextLong();
			this.value38 = rnd.nextLong();
			this.value09 = rnd.nextLong();
			this.value19 = rnd.nextLong();
			this.value29 = rnd.nextLong();
			this.value39 = rnd.nextLong();
			this.value10 = rnd.nextLong();
			this.value20 = rnd.nextLong();
			this.value30 = rnd.nextLong();
			this.value40 = rnd.nextLong();
		}

	}

	public static class Tuple50 {
		public long value01, value11, value21, value31, value41;
		public long value02, value12, value22, value32, value42;
		public long value03, value13, value23, value33, value43;
		public long value04, value14, value24, value34, value44;
		public long value05, value15, value25, value35, value45;
		public long value06, value16, value26, value36, value46;
		public long value07, value17, value27, value37, value47;
		public long value08, value18, value28, value38, value48;
		public long value09, value19, value29, value39, value49;
		public long value10, value20, value30, value40, value50;

		public Tuple50( final Random rnd ) {
			this.value01 = rnd.nextLong();
			this.value11 = rnd.nextLong();
			this.value21 = rnd.nextLong();
			this.value31 = rnd.nextLong();
			this.value41 = rnd.nextLong();
			this.value02 = rnd.nextLong();
			this.value12 = rnd.nextLong();
			this.value22 = rnd.nextLong();
			this.value32 = rnd.nextLong();
			this.value42 = rnd.nextLong();
			this.value03 = rnd.nextLong();
			this.value13 = rnd.nextLong();
			this.value23 = rnd.nextLong();
			this.value33 = rnd.nextLong();
			this.value43 = rnd.nextLong();
			this.value04 = rnd.nextLong();
			this.value14 = rnd.nextLong();
			this.value24 = rnd.nextLong();
			this.value34 = rnd.nextLong();
			this.value44 = rnd.nextLong();
			this.value05 = rnd.nextLong();
			this.value15 = rnd.nextLong();
			this.value25 = rnd.nextLong();
			this.value35 = rnd.nextLong();
			this.value45 = rnd.nextLong();
			this.value06 = rnd.nextLong();
			this.value16 = rnd.nextLong();
			this.value26 = rnd.nextLong();
			this.value36 = rnd.nextLong();
			this.value46 = rnd.nextLong();
			this.value07 = rnd.nextLong();
			this.value17 = rnd.nextLong();
			this.value27 = rnd.nextLong();
			this.value37 = rnd.nextLong();
			this.value47 = rnd.nextLong();
			this.value08 = rnd.nextLong();
			this.value18 = rnd.nextLong();
			this.value28 = rnd.nextLong();
			this.value38 = rnd.nextLong();
			this.value48 = rnd.nextLong();
			this.value09 = rnd.nextLong();
			this.value19 = rnd.nextLong();
			this.value29 = rnd.nextLong();
			this.value39 = rnd.nextLong();
			this.value49 = rnd.nextLong();
			this.value10 = rnd.nextLong();
			this.value20 = rnd.nextLong();
			this.value30 = rnd.nextLong();
			this.value40 = rnd.nextLong();
			this.value50 = rnd.nextLong();
		}

		public Tuple50( final long value01, final long value11, final long value21, final long value31, final long value41, final long value02, final long value12, final long value22, final long value32, final long value42, final long value03, final long value13, final long value23, final long value33, final long value43, final long value04, final long value14, final long value24, final long value34, final long value44, final long value05, final long value15, final long value25, final long value35, final long value45, final long value06, final long value16, final long value26, final long value36, final long value46, final long value07, final long value17, final long value27, final long value37, final long value47, final long value08, final long value18, final long value28, final long value38, final long value48, final long value09, final long value19, final long value29, final long value39, final long value49, final long value10, final long value20, final long value30, final long value40, final long value50 ) {
			this.value01 = value01;
			this.value11 = value11;
			this.value21 = value21;
			this.value31 = value31;
			this.value41 = value41;
			this.value02 = value02;
			this.value12 = value12;
			this.value22 = value22;
			this.value32 = value32;
			this.value42 = value42;
			this.value03 = value03;
			this.value13 = value13;
			this.value23 = value23;
			this.value33 = value33;
			this.value43 = value43;
			this.value04 = value04;
			this.value14 = value14;
			this.value24 = value24;
			this.value34 = value34;
			this.value44 = value44;
			this.value05 = value05;
			this.value15 = value15;
			this.value25 = value25;
			this.value35 = value35;
			this.value45 = value45;
			this.value06 = value06;
			this.value16 = value16;
			this.value26 = value26;
			this.value36 = value36;
			this.value46 = value46;
			this.value07 = value07;
			this.value17 = value17;
			this.value27 = value27;
			this.value37 = value37;
			this.value47 = value47;
			this.value08 = value08;
			this.value18 = value18;
			this.value28 = value28;
			this.value38 = value38;
			this.value48 = value48;
			this.value09 = value09;
			this.value19 = value19;
			this.value29 = value29;
			this.value39 = value39;
			this.value49 = value49;
			this.value10 = value10;
			this.value20 = value20;
			this.value30 = value30;
			this.value40 = value40;
			this.value50 = value50;
		}
	}

	public static class Tuple51 {
		public long value01, value11, value21, value31, value41, value51;
		public long value02, value12, value22, value32, value42;
		public long value03, value13, value23, value33, value43;
		public long value04, value14, value24, value34, value44;
		public long value05, value15, value25, value35, value45;
		public long value06, value16, value26, value36, value46;
		public long value07, value17, value27, value37, value47;
		public long value08, value18, value28, value38, value48;
		public long value09, value19, value29, value39, value49;
		public long value10, value20, value30, value40, value50;

		public Tuple51( final long value01, final long value11, final long value21, final long value31, final long value41, final long value51, final long value02, final long value12, final long value22, final long value32, final long value42, final long value03, final long value13, final long value23, final long value33, final long value43, final long value04, final long value14, final long value24, final long value34, final long value44, final long value05, final long value15, final long value25, final long value35, final long value45, final long value06, final long value16, final long value26, final long value36, final long value46, final long value07, final long value17, final long value27, final long value37, final long value47, final long value08, final long value18, final long value28, final long value38, final long value48, final long value09, final long value19, final long value29, final long value39, final long value49, final long value10, final long value20, final long value30, final long value40, final long value50 ) {
			this.value01 = value01;
			this.value11 = value11;
			this.value21 = value21;
			this.value31 = value31;
			this.value41 = value41;
			this.value51 = value51;
			this.value02 = value02;
			this.value12 = value12;
			this.value22 = value22;
			this.value32 = value32;
			this.value42 = value42;
			this.value03 = value03;
			this.value13 = value13;
			this.value23 = value23;
			this.value33 = value33;
			this.value43 = value43;
			this.value04 = value04;
			this.value14 = value14;
			this.value24 = value24;
			this.value34 = value34;
			this.value44 = value44;
			this.value05 = value05;
			this.value15 = value15;
			this.value25 = value25;
			this.value35 = value35;
			this.value45 = value45;
			this.value06 = value06;
			this.value16 = value16;
			this.value26 = value26;
			this.value36 = value36;
			this.value46 = value46;
			this.value07 = value07;
			this.value17 = value17;
			this.value27 = value27;
			this.value37 = value37;
			this.value47 = value47;
			this.value08 = value08;
			this.value18 = value18;
			this.value28 = value28;
			this.value38 = value38;
			this.value48 = value48;
			this.value09 = value09;
			this.value19 = value19;
			this.value29 = value29;
			this.value39 = value39;
			this.value49 = value49;
			this.value10 = value10;
			this.value20 = value20;
			this.value30 = value30;
			this.value40 = value40;
			this.value50 = value50;
		}

		public Tuple51( final Random rnd ) {
			this.value01 = rnd.nextLong();
			this.value11 = rnd.nextLong();
			this.value21 = rnd.nextLong();
			this.value31 = rnd.nextLong();
			this.value41 = rnd.nextLong();
			this.value51 = rnd.nextLong();
			this.value02 = rnd.nextLong();
			this.value12 = rnd.nextLong();
			this.value22 = rnd.nextLong();
			this.value32 = rnd.nextLong();
			this.value42 = rnd.nextLong();
			this.value03 = rnd.nextLong();
			this.value13 = rnd.nextLong();
			this.value23 = rnd.nextLong();
			this.value33 = rnd.nextLong();
			this.value43 = rnd.nextLong();
			this.value04 = rnd.nextLong();
			this.value14 = rnd.nextLong();
			this.value24 = rnd.nextLong();
			this.value34 = rnd.nextLong();
			this.value44 = rnd.nextLong();
			this.value05 = rnd.nextLong();
			this.value15 = rnd.nextLong();
			this.value25 = rnd.nextLong();
			this.value35 = rnd.nextLong();
			this.value45 = rnd.nextLong();
			this.value06 = rnd.nextLong();
			this.value16 = rnd.nextLong();
			this.value26 = rnd.nextLong();
			this.value36 = rnd.nextLong();
			this.value46 = rnd.nextLong();
			this.value07 = rnd.nextLong();
			this.value17 = rnd.nextLong();
			this.value27 = rnd.nextLong();
			this.value37 = rnd.nextLong();
			this.value47 = rnd.nextLong();
			this.value08 = rnd.nextLong();
			this.value18 = rnd.nextLong();
			this.value28 = rnd.nextLong();
			this.value38 = rnd.nextLong();
			this.value48 = rnd.nextLong();
			this.value09 = rnd.nextLong();
			this.value19 = rnd.nextLong();
			this.value29 = rnd.nextLong();
			this.value39 = rnd.nextLong();
			this.value49 = rnd.nextLong();
			this.value10 = rnd.nextLong();
			this.value20 = rnd.nextLong();
			this.value30 = rnd.nextLong();
			this.value40 = rnd.nextLong();
			this.value50 = rnd.nextLong();
		}
	}

	public static class Tuple64 {
		public long value01, value11, value21, value31, value41, value51, value61;
		public long value02, value12, value22, value32, value42, value52, value62;
		public long value03, value13, value23, value33, value43, value53, value63;
		public long value04, value14, value24, value34, value44, value54, value64;
		public long value05, value15, value25, value35, value45, value55;
		public long value06, value16, value26, value36, value46, value56;
		public long value07, value17, value27, value37, value47, value57;
		public long value08, value18, value28, value38, value48, value58;
		public long value09, value19, value29, value39, value49, value59;
		public long value10, value20, value30, value40, value50, value60;

		public Tuple64( final long value01, final long value11, final long value21, final long value31, final long value41, final long value51, final long value61, final long value02, final long value12, final long value22, final long value32, final long value42, final long value52, final long value62, final long value03, final long value13, final long value23, final long value33, final long value43, final long value53, final long value63, final long value04, final long value14, final long value24, final long value34, final long value44, final long value54, final long value64, final long value05, final long value15, final long value25, final long value35, final long value45, final long value55, final long value06, final long value16, final long value26, final long value36, final long value46, final long value56, final long value07, final long value17, final long value27, final long value37, final long value47, final long value57, final long value08, final long value18, final long value28, final long value38, final long value48, final long value58, final long value09, final long value19, final long value29, final long value39, final long value49, final long value59, final long value10, final long value20, final long value30, final long value40, final long value50, final long value60 ) {
			this.value01 = value01;
			this.value11 = value11;
			this.value21 = value21;
			this.value31 = value31;
			this.value41 = value41;
			this.value51 = value51;
			this.value61 = value61;
			this.value02 = value02;
			this.value12 = value12;
			this.value22 = value22;
			this.value32 = value32;
			this.value42 = value42;
			this.value52 = value52;
			this.value62 = value62;
			this.value03 = value03;
			this.value13 = value13;
			this.value23 = value23;
			this.value33 = value33;
			this.value43 = value43;
			this.value53 = value53;
			this.value63 = value63;
			this.value04 = value04;
			this.value14 = value14;
			this.value24 = value24;
			this.value34 = value34;
			this.value44 = value44;
			this.value54 = value54;
			this.value64 = value64;
			this.value05 = value05;
			this.value15 = value15;
			this.value25 = value25;
			this.value35 = value35;
			this.value45 = value45;
			this.value55 = value55;
			this.value06 = value06;
			this.value16 = value16;
			this.value26 = value26;
			this.value36 = value36;
			this.value46 = value46;
			this.value56 = value56;
			this.value07 = value07;
			this.value17 = value17;
			this.value27 = value27;
			this.value37 = value37;
			this.value47 = value47;
			this.value57 = value57;
			this.value08 = value08;
			this.value18 = value18;
			this.value28 = value28;
			this.value38 = value38;
			this.value48 = value48;
			this.value58 = value58;
			this.value09 = value09;
			this.value19 = value19;
			this.value29 = value29;
			this.value39 = value39;
			this.value49 = value49;
			this.value59 = value59;
			this.value10 = value10;
			this.value20 = value20;
			this.value30 = value30;
			this.value40 = value40;
			this.value50 = value50;
			this.value60 = value60;
		}

		public Tuple64( final Random rnd ) {
			this.value01 = rnd.nextLong();
			this.value11 = rnd.nextLong();
			this.value21 = rnd.nextLong();
			this.value31 = rnd.nextLong();
			this.value41 = rnd.nextLong();
			this.value51 = rnd.nextLong();
			this.value61 = rnd.nextLong();
			this.value02 = rnd.nextLong();
			this.value12 = rnd.nextLong();
			this.value22 = rnd.nextLong();
			this.value32 = rnd.nextLong();
			this.value42 = rnd.nextLong();
			this.value52 = rnd.nextLong();
			this.value62 = rnd.nextLong();
			this.value03 = rnd.nextLong();
			this.value13 = rnd.nextLong();
			this.value23 = rnd.nextLong();
			this.value33 = rnd.nextLong();
			this.value43 = rnd.nextLong();
			this.value53 = rnd.nextLong();
			this.value63 = rnd.nextLong();
			this.value04 = rnd.nextLong();
			this.value14 = rnd.nextLong();
			this.value24 = rnd.nextLong();
			this.value34 = rnd.nextLong();
			this.value44 = rnd.nextLong();
			this.value54 = rnd.nextLong();
			this.value64 = rnd.nextLong();
			this.value05 = rnd.nextLong();
			this.value15 = rnd.nextLong();
			this.value25 = rnd.nextLong();
			this.value35 = rnd.nextLong();
			this.value45 = rnd.nextLong();
			this.value55 = rnd.nextLong();
			this.value06 = rnd.nextLong();
			this.value16 = rnd.nextLong();
			this.value26 = rnd.nextLong();
			this.value36 = rnd.nextLong();
			this.value46 = rnd.nextLong();
			this.value56 = rnd.nextLong();
			this.value07 = rnd.nextLong();
			this.value17 = rnd.nextLong();
			this.value27 = rnd.nextLong();
			this.value37 = rnd.nextLong();
			this.value47 = rnd.nextLong();
			this.value57 = rnd.nextLong();
			this.value08 = rnd.nextLong();
			this.value18 = rnd.nextLong();
			this.value28 = rnd.nextLong();
			this.value38 = rnd.nextLong();
			this.value48 = rnd.nextLong();
			this.value58 = rnd.nextLong();
			this.value09 = rnd.nextLong();
			this.value19 = rnd.nextLong();
			this.value29 = rnd.nextLong();
			this.value39 = rnd.nextLong();
			this.value49 = rnd.nextLong();
			this.value59 = rnd.nextLong();
			this.value10 = rnd.nextLong();
			this.value20 = rnd.nextLong();
			this.value30 = rnd.nextLong();
			this.value40 = rnd.nextLong();
			this.value50 = rnd.nextLong();
			this.value60 = rnd.nextLong();
		}
	}


	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, 1, 2, 32, 44, 50, 51, 64 ),
		        asList(
				        new JvmExtendedProperty( "FreqInlineSize", "325"  /*default*/),
				        new JvmExtendedProperty( "FreqInlineSize", "1000" )
		        )
		);
	}
}
