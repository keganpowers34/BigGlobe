package builderb0y.scripting.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class ArrayBuilder<T> extends ArrayList<T> implements Consumer<T> {

	public ArrayBuilder() {}

	public ArrayBuilder(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public void accept(T t) {
		this.add(t);
	}

	@SafeVarargs
	public final void add(T... elements) {
		Collections.addAll(this, elements);
	}

	@SafeVarargs
	public final ArrayBuilder<T> append(T... elements) {
		this.add(elements);
		return this;
	}

	public ArrayBuilder<T> append(T element) {
		this.add(element);
		return this;
	}

	public ArrayBuilder<T> append(T element, int count) {
		for (int loop = 0; loop < count; loop++) {
			this.add(element);
		}
		return this;
	}

	@Override
	public <T1> T1[] toArray(IntFunction<T1[]> generator) {
		return this.toArray(generator.apply(this.size()));
	}
}