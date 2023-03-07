package builderb0y.scripting.environments;

import java.util.*;

import builderb0y.scripting.bytecode.MethodInfo;
import builderb0y.scripting.bytecode.TypeInfo;
import builderb0y.scripting.bytecode.tree.InsnTree;
import builderb0y.scripting.bytecode.tree.InsnTree.CastMode;
import builderb0y.scripting.bytecode.tree.instructions.InvokeInsnTree;
import builderb0y.scripting.parsing.ExpressionParser;
import builderb0y.scripting.parsing.ScriptParsingException;
import builderb0y.scripting.util.TypeInfos;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class JavaUtilScriptEnvironment {

	public static final MutableScriptEnvironment ALL = (
		new MutableScriptEnvironment()
		.addType("Object", Object.class)
		.addMethodInvokes(Object.class, "toString", "equals", "hashCode", "getClass")
		.addType("Iterator", Iterator.class)
		.addMethodInvokes(Iterator.class, "hasNext", "next", "remove")
		.addType("ListIterator", ListIterator.class)
		.addMethodInvokes(ListIterator.class, "hasPrevious", "previous", "nextIndex", "previousIndex", "set", "add")
		.addType("Map", Map.class)
		.addType("MapEntry", Map.Entry.class)
		.addMethodMultiInvokes(Map.class, "size", "isEmpty", "containsKey", "containsValue", "get", "put", "remove", "putAll", "clear", "keySet", "values", "entrySet", "getOrDefault", "putIfAbsent", "replace")
		.addFieldInvokes(Map.class, "size", "isEmpty")
		.addMethodInvokes(Map.Entry.class, "getKey", "getValue", "setValue")
		.addMethod(TypeInfo.of(Map.class), "", (parser, receiver, name, arguments) -> {
			InsnTree key = ScriptEnvironment.castArgument(parser, "", TypeInfos.OBJECT, CastMode.IMPLICIT_THROW, arguments);
			return new MapGetInsnTree(receiver, key);
		})
		.addType("SortedMap", SortedMap.class)
		.addMethodInvokes(SortedMap.class, "firstKey", "lastKey")
		.addType("NavigableMap", NavigableMap.class)
		.addMethodMultiInvokes(NavigableMap.class, "lowerEntry", "lowerKey", "floorEntry", "floorKey", "ceilingEntry", "ceilingKey", "higherEntry", "higherKey", "firstEntry", "lastEntry", "pollFirstEntry", "pollLastEntry", "descendingMap", "navigableKeySet", "descendingKeySet", "subMap", "headMap", "tailMap")
		.addType("TreeMap", TreeMap.class)
		.addQualifiedSpecificConstructor(TreeMap.class, SortedMap.class)
		.addQualifiedSpecificConstructor(TreeMap.class, Map.class)
		.addQualifiedSpecificConstructor(TreeMap.class)
		.addType("HashMap", HashMap.class)
		.addQualifiedMultiConstructor(HashMap.class)
		.addType("LinkedHashMap", LinkedHashMap.class)
		.addQualifiedMultiConstructor(LinkedHashMap.class)
		.addType("Iterable", Iterable.class)
		.addMethodInvoke(Iterable.class, "iterator")
		.addType("Collection", Collection.class)
		.addMethodInvokes(Collection.class, "size", "isEmpty", "contains", "add", "containsAll", "addAll", "removeAll", "retainAll", "clear")
		.addMethodRenamedInvoke("removeElement", Collection.class, "remove")
		.addFieldInvokes(Collection.class, "size", "isEmpty")
		.addType("Set", Set.class)
		.addType("SortedSet", SortedSet.class)
		.addMethodInvokes(SortedSet.class, "subSet", "headSet", "tailSet", "first", "last")
		.addType("NavigableSet", NavigableSet.class)
		.addMethodMultiInvokes(NavigableSet.class, "lower", "floor", "ceiling", "higher", "pollFirst", "pollLast", "descendingSet", "descendingIterator", "subSet", "headSet", "tailSet")
		.addType("TreeSet", TreeSet.class)
		.addQualifiedSpecificConstructor(TreeSet.class, SortedSet.class)
		.addQualifiedSpecificConstructor(TreeSet.class, Collection.class)
		.addQualifiedSpecificConstructor(TreeSet.class)
		.addType("HashSet", HashSet.class)
		.addQualifiedSpecificConstructor(HashSet.class)
		.addQualifiedSpecificConstructor(HashSet.class, int.class)
		.addQualifiedSpecificConstructor(HashSet.class, Collection.class)
		.addQualifiedSpecificConstructor(HashSet.class, int.class, float.class)
		.addType("LinkedHashSet", LinkedHashSet.class)
		.addQualifiedSpecificConstructor(LinkedHashSet.class)
		.addQualifiedSpecificConstructor(LinkedHashSet.class, int.class)
		.addQualifiedSpecificConstructor(LinkedHashSet.class, Collection.class)
		.addQualifiedSpecificConstructor(LinkedHashSet.class, int.class, float.class)
		.addType("List", List.class)
		.addMethodMultiInvokes(List.class, "addAll", "add", "get", "set", "indexOf", "lastIndexOf", "listIterator", "subList")
		.addMethodRenamedInvokeSpecific("removeIndex", List.class, "remove", Object.class, int.class)
		.addMethod(TypeInfo.of(List.class), "", (parser, receiver, name, arguments) -> {
			InsnTree index = ScriptEnvironment.castArgument(parser, "", TypeInfos.INT, CastMode.IMPLICIT_THROW, arguments);
			return new ListGetInsnTree(receiver, index);
		})
		.addType("LinkedList", LinkedList.class)
		.addQualifiedMultiConstructor(LinkedList.class)
		.addType("ArrayList", ArrayList.class)
		.addQualifiedMultiConstructor(ArrayList.class)
		.addMethodInvokes(ArrayList.class, "trimToSize", "ensureCapacity")
		.addType("Queue", Queue.class)
		.addMethodInvokes(Queue.class, "offer", "remove", "poll", "element", "peek")
		.addType("Deque", Deque.class)
		.addMethodInvokes(Deque.class, "addFirst", "addLast", "offerFirst", "offerLast", "removeFirst", "removeLast", "pollFirst", "pollLast", "getFirst", "getLast", "peekFirst", "peekLast", "removeFirstOccurrence", "removeLastOccurrence", "push", "pop")
		.addType("ArrayDeque", ArrayDeque.class)
		.addQualifiedMultiConstructor(ArrayDeque.class)
		.addType("PriorityQueue", PriorityQueue.class)
		.addQualifiedMultiConstructor(PriorityQueue.class)
	);

	public static class MapGetInsnTree extends InvokeInsnTree {

		public static final MethodInfo
			GET = MethodInfo.getMethod(Map.class, "get"),
			PUT = MethodInfo.getMethod(Map.class, "put");

		public MapGetInsnTree(InsnTree map, InsnTree key) {
			super(INVOKEINTERFACE, map, GET, key);
		}

		@Override
		public InsnTree update(ExpressionParser parser, UpdateOp op, InsnTree rightValue) throws ScriptParsingException {
			if (op == UpdateOp.ASSIGN) {
				return invokeInterface(this.receiver, PUT, this.args[0], rightValue.cast(parser, TypeInfos.OBJECT, CastMode.IMPLICIT_THROW)).cast(parser, TypeInfos.VOID, CastMode.EXPLICIT_THROW);
			}
			throw new ScriptParsingException("Updating Map not yet implemented", parser.input);
		}
	}

	public static class ListGetInsnTree extends InvokeInsnTree {

		public static final MethodInfo
			GET = MethodInfo.getMethod(List.class, "get"),
			SET = MethodInfo.getMethod(List.class, "set");

		public ListGetInsnTree(InsnTree list, InsnTree index) {
			super(INVOKEINTERFACE, list, GET, index);
		}

		@Override
		public InsnTree update(ExpressionParser parser, UpdateOp op, InsnTree rightValue) throws ScriptParsingException {
			if (op == UpdateOp.ASSIGN) {
				return invokeInterface(this.receiver, SET, this.args[0], rightValue.cast(parser, TypeInfos.OBJECT, CastMode.IMPLICIT_THROW)).cast(parser, TypeInfos.VOID, CastMode.EXPLICIT_THROW);
			}
			throw new ScriptParsingException("Updating List not yet implemented", parser.input);
		}
	}
}