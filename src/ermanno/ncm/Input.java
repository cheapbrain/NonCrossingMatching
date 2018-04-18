package ermanno.ncm;

import java.util.Random;

public class Input {
	public SubArray a, b;
	public SubArray[] indA, indB;

	public Input(SubArray a, SubArray b) {
		this.a = a;
		this.b = b;

		indA = new SubArray[4];
		indB = new SubArray[4];

		int[] countsA = new int[4];
		int[] countsB = new int[4];

		for (int i=0; i<a.length; i++) countsA[a.get(i)]++;
		for (int i=0; i<4; i++) indA[i] = new SubArray(new int[countsA[i]]);
		for (int i=0, j[]=new int[4]; i<a.length; i++) indA[a.get(i)].set(j[a.get(i)]++, i);

		for (int i=0; i<b.length; i++) countsB[b.get(i)]++;
		for (int i=0; i<4; i++) indB[i] = new SubArray(new int[countsB[i]]);
		for (int i=0, j[]=new int[4]; i<b.length; i++) indB[b.get(i)].set(j[b.get(i)]++, i);
	}

	public Input(SubArray a, SubArray b, SubArray[] indA, SubArray[] indB) {
		this.a = a;
		this.b = b;
		this.indA = indA;
		this.indB = indB;
	}

	public Input advance(int a, int b) {
		SubArray[] indA = new SubArray[4];
		SubArray[] indB = new SubArray[4];
		for (int i = 0; i < 4; i++) {
			indA[i] = this.indA[i].discardLesserThan(this.a.start + a);
			indB[i] = this.indB[i].discardLesserThan(this.b.start + b);
		}

		return new Input(this.a.trim(a), this.b.trim(b), indA, indB);
	}
	
	public Input advanceBack(int a, int b) {
		SubArray[] indA = new SubArray[4];
		SubArray[] indB = new SubArray[4];
		for (int i = 0; i < 4; i++) {
			indA[i] = this.indA[i].discardGreaterThan(this.a.start + a);
			indB[i] = this.indB[i].discardGreaterThan(this.b.start + b);
		}

		return new Input(this.a.trimBack(a+1), this.b.trimBack(b+1), indA, indB);
	}

	private static int[] randomArray(int n) {
		int[] a = new int[n];
		Random rng = new Random();
		for (int i = 0; i < a.length; i++) { a[i] = (int) rng.nextInt(4); }
		return a;
	}

	public static Input random(int n) {
		int[] a = randomArray(n);
		int[] b = randomArray(n);
		return new Input(new SubArray(a), new SubArray(b));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<a.length; i++) builder.append(a.get(i));
		builder.append("\n");
		for(int i=0; i<b.length; i++) builder.append(b.get(i));
		return builder.toString();
	}
}
