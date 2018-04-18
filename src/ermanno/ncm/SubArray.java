package ermanno.ncm;

public class SubArray {
	private final int[] array;
	public final int start;
	public final int length;

	public SubArray(int[] array) {
		this.array = array;
		this.start = 0;
		this.length = array.length;
	}

	public SubArray(int[] array, int start) {
		this.array = array;
		this.start = start;
		this.length = array.length - start;
	}

	public SubArray(int[] array, int start, int length) {
		this.array = array;
		this.start = start;
		this.length = length;
	}

	public void set(int index, int value) {
		array[start + index] = value;
	}

	public int get(int index) {
		return array[start + index];
	}

	public SubArray trim(int start, int length) {
		return new SubArray(array, this.start + start, length);
	}

	public SubArray trim(int start) {
		return trim(start, length - start);
	}

	public SubArray discardLesserThan(int th) {
		int i;
		for (i = 0; i < length; i++) {
			if (array[start + i] >= th) break;
		}
		return trim(i);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("{");
		for (int i = start; i < start + length; i++) {
			if (i > start) out.append(", ");
			out.append(array[i]);
		}
		out.append("}");
		return out.toString();
	}
}
