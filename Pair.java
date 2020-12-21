public class Pair<K, V> {
	private V v;
	private K k;

	public Pair(K k, V v) {
		this.k = k;
		this.v = v;
	}

	public V getValue() {
		return this.v;
	}

	public V setValue(V v) {
		this.v = v;
		return this.v;
	}

	public K getKey() {
		return this.k;
	}

	public K setKey(K k) {
		this.k = k;
		return this.k;
	}

}
