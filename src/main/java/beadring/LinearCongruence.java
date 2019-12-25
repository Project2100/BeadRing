/*
 * MIT License
 *
 * Copyright (c) 2018 Andrea Proietto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package beadring;

import java.io.PrintStream;

/**
 *
 * @author Project2100
 */
public class LinearCongruence {

	static class Radix {

		public Radix(int value, int baseMod, int period) {
			this.baseMod = baseMod;
			this.value = value;
			this.period = period;
		}

		int baseMod, value, period;

		@Override
		public String toString() {
			return "x = " + value + " (+ " + baseMod + "k), k: 0 -> " + (period - 1);
		}
	}

	int coefficient;
	int known;
	int modulus;

	public LinearCongruence(int coefficient, int known, int modulus) {
		if (modulus == 0) {
			throw new IllegalArgumentException("Modulus cannot be 0");
		}
		this.coefficient = coefficient;
		this.known = known;
		this.modulus = modulus > 0 ? modulus : -modulus;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof LinearCongruence)) return false;
		LinearCongruence o = (LinearCongruence) obj;
		return o.coefficient == coefficient && o.known == known && o.modulus == modulus; //To change body of generated methods, choose Tools | Templates.
	}

	/**
	 * @brief canonize
	 * @param term
	 * @param modulus
	 * @return
	 */
	static int canonize(int term, int modulus) {
		if (modulus <= 0) modulus *= -1;
		return term - (term / modulus * modulus);
	}

	/**
	 * @brief canonize
	 * @return
	 * @deprecated The static variant {@link #canonize(int, int)} should be preferred over this one
	 */
	@Deprecated
	LinearCongruence canonize() {
		return (coefficient >= 0 && coefficient < modulus && known >= 0 && known < modulus)
				? this
				: new LinearCongruence(Math.floorMod(coefficient, modulus), Math.floorMod(known, modulus), modulus);
	}

	/**
	 * Original algorithm:
	 *
	 * int gcd(int a, int b) {
	 *
	 * int remainder = a > b ? a : b; int divisor = a < b ? a : b;
	 *
	 * int quotient;
	 * while (divisor != 0) {
	 *
	 * // Find quotient of a/b
	 * quotient = 0;
	 * while (remainder >= divisor) { remainder -= divisor; quotient++; }
	 *
	 * // ac now contains the remainder: a = b*quotient + ac
	 *
	 * // Assertion: b > ac (see while guard)
	 *
	 * // Now b is lesser than a, swap them and continue
	 *
	 * int newDivisor = remainder; remainder = divisor; divisor = newDivisor; }
	 * // repeat until remainder is zero, b will be the gcd
	 *
	 * return remainder; // Divisor has been swapped with remainder }
	 *
	 * Euclid's algorithm implementation
	 *
	 * @param a first operand
	 * @param b second operand
	 * @return the greatest common divisor of a and b
	 */

	static int gcd(int a, int b) {
		if (a < 0 || b < 0 || (a == 0 && b == 0)) return 0;

		// Guard being true means we found the gcd
		while (a != 0) {

			// Obtain the remainder
			b %= a;

			// Fast swap, duplicate guard
			if (b == 0) return a;
			else a %= b;
		}

		return b;
	}


	/**
	 * @brief findMultInverse Returns the multiplicative inverse of term modulo
	 * modulus, or else -1
	 * @param term
	 * @param modulus
	 * @return
	 */
	static int findMultInverse(int term, int modulus) {
		return fmiclean(term, modulus, 2);
	}

//	/**
//	 * @brief reduce Reduces the given congruence, if solvable, to its coprime
//	 * form.
//	 * @param c
//	 * @return
//	 */
//	@Deprecated
//	LinearCongruence reduce() {
//
//		// A linear congruence is solvable iff gcd(coeff, modulus) divides known
//		// -> Thus, if the former is false, the congruence is unsolvable
//		int commondiv = gcd(coefficient, modulus);
//		if ((known % commondiv) != 0) return null;
//
//		// Else, if gcd is greater than 1 we reduce the congruence
//		return commondiv == 1
//				? this
//				: new LinearCongruence(coefficient / commondiv, known / commondiv, modulus / commondiv);
//
//	}


	/**
	 * Old algorithm:
	 *
	 *
	 *
	 * int product = 0; int x = 0;
	 *
	 * // ASSERT: x < modulus ALWAYS, loop SHALL eventually terminate
	 * while (product != reduced.known) {
	 * product += reduced.coefficient;
	 * x++;
	 * if (product >= reduced.modulus) { product -= reduced.modulus; } }
	 *
	 *
	 *
	 *
	 * @param c
	 * @param log
	 * @return
	 */
	static Radix solveLinearCongruence(LinearCongruence c, PrintStream log) {
		log.println("Solving " + c);

		LinearCongruence reduced = c.canonize();
		if (c != reduced) {
			log.println("Simplified as: " + reduced);
		}

		// Reduce the congruence, if possible
		int commondiv = gcd(reduced.coefficient, reduced.modulus);
		if ((reduced.known % commondiv) != 0) {
			log.println("Congruence is unsolvable, GCD" + commondiv + "does not divide" + reduced.known);
			return null;
		}
		else if (commondiv != 1) {
			reduced = new LinearCongruence(reduced.coefficient / commondiv, reduced.known / commondiv, reduced.modulus / commondiv);
			log.println("Reduced form: " + reduced + ", GCD: " + commondiv);
		}

		Radix r = new Radix(
				(findMultInverse(reduced.coefficient, reduced.modulus) * reduced.known) % reduced.modulus,
				reduced.modulus,
				(c.modulus / reduced.modulus));

		log.println("Solution: " + r);
		return r;
	}


	static Radix solveCongruenceSystem(LinearCongruence[] congs, int count, PrintStream log) {

		// Solve first
		log.print("Congruence no.0: ");
		Radix r = solveLinearCongruence(congs[0], log);
		log.println();

		for (int idx = 1; idx < count; idx++) {

			LinearCongruence cong = congs[idx];
			log.print("Congruence no." + (idx) + ": ");

			// Check if moduli are coprime
			if (gcd(r.baseMod, cong.modulus) != 1) {
				log.println("Modulus (" + cong.modulus + ") is not coprime with the preceding ones!\nCurrent radix is set in modulo " + r.baseMod);
				return null;
			}

			Radix r2 = solveLinearCongruence(cong, log);
			log.println();

			// Find common solution
			int nextMod = r.baseMod * r2.baseMod;
			int a = r.value;
			int b = r2.value;
			while (a != b) {
				if (a > nextMod || b > nextMod) {

					// If we ever get here, something's HORRIBLY WRONG
					throw new RuntimeException("INTERNAL ERROR: Count exceeded");
				}
				// TODO ternary operator
				else if (a > b) {
					b += r2.baseMod;
				}
				else { // a<b
					a += r.baseMod;
				}
			}

			r.baseMod = nextMod;
			r.period *= gcd(cong.coefficient, cong.modulus);
			r.value = a;
			log.println("Common result: " + r + "\n");
		}

		return r;
	}

	@Override
	public String toString() {
		return coefficient + "x ~ " + known + " (mod " + modulus + ")";
	}

	/**
	 *
	 * @param term
	 * @param modulus
	 * @return
	 */
	static int cbs(int term, int modulus) {

		if (term == 1) {
			System.out.println("Base case (1)");
			return 1;
		}
		else if (term == (modulus - 1)) {
			System.out.println("Base case (n - 1)");
			return term;
		}
		else if (term == 2) {
			System.out.println("Base case (2), inverse = (n + 1) / 2");
			return (modulus + 1) / 2;
		}
		else if (term == modulus - 2) {
			System.out.println("Base case (n - 2), inverse = (n - 1) / 2");
			return (modulus - 1) / 2;
		}
		else if (term == 3) {
			System.out.println("Base case (3), inverse = (n * (3 - alpha) + 1) / 3");
			return (modulus * (3 - (modulus % 3)) + 1) / 3;
		}
		else if (term == modulus - 3) {
			System.out.println("Base case (n - 3), inverse = (n * alpha - 1) / 3");
			return (modulus * (modulus % 3) - 1) / 3;
		}
		// A parity check is needed now!
//		else if (term == (modulus + 1) / 2 && (modulus & 1) != 0) {
//			return 2;
//		}
//		else if (term == (modulus - 1) / 2 && (modulus & 1) != 0) {
//			return modulus - 2;
//		}
		else return 0;
	}

	/**
	 * Naive implementation for finding the modular multiplicative inverse:
	 *
	 * multiply the term by itself until it is congruent to 1 and count these
	 * times; the count itself is the inverse.
	 *
	 * @param term
	 * @param modulus
	 * @return
	 */
	static int fmi1(int term, int modulus) {
		if (modulus == 0) return -1;
		if (term > modulus) term = canonize(term, modulus);

		int product = 0;
		int x = 0;

		do {
			// Add a "time" to the product, and check if it goes over modulus
			product += term;
			if (product > modulus) product -= modulus; // 1-canonization, no need for complex checks
			x++;

			// This happens if 'term' is not coprime with 'modulus')
			if (x > modulus) return -1;
		} while (product != 1);

		return x;
	}


	/**
	 * Better implementation, recursive, uses the following formula:
	 *
	 * Inv(term, modulus) = (modulus * Inv(term - a, term) + 1 ) / term
	 *
	 * where 'a' is the remainder of modulus divided by term
	 *
	 * NOTE: complexity may still be linear
	 *
	 *
	 * @param term
	 * @param modulus
	 * @return
	 */
	static int fmi2(int term, int modulus) {

		System.out.format("--CALL: %d, %d\n", term, modulus);
		if (modulus == 0 || gcd(term, modulus) != 1) return -1;
		if (term > modulus) {
			term = canonize(term, modulus);
			System.out.println("term canonized to " + term);
		}

		int res = cbs(term, modulus);
		if (res != 0) return res;

		int alpha = modulus % term;

		res = (modulus * fmi2(term - alpha, term) + 1) / term;

		System.out.format("Inv(%d, %d) = (%d * Inv(%d - %d, %d) + 1) / %d\n", term, modulus, modulus, term, alpha, term, term);
		System.out.format("EXPECTED: %d, FOUND: %d\n", findMultInverse(term, modulus), res);

		return res;


	}

	/**
	 * Best implementation so far, takes advantage of multiple alpha-constant
	 * steps
	 *
	 * Formula: Inv(term, modulus) = (modulus * Inv(k - a, k) + 1 + x * mi ) / k
	 *
	 * @param term
	 * @param modulus
	 * @return
	 */
	static int fmi3(int term, int modulus) {

		System.out.format("--CALL: %d, %d\n", term, modulus);
		if (modulus == 0 || gcd(term, modulus) != 1) return -1;
		if (term > modulus) {
			term = canonize(term, modulus);
			System.out.println("term canonized to " + term);
		}

		int res = cbs(term, modulus);
		if (res != 0) return res;


		// START : term = 2071 = k+alpha*n, modulus = 6735 = OMEGA
		// OMEGA % term = alpha
		int alpha = modulus % term;

		// NOW: alpha, omega, term

		// If alpha = 1, the formula is greatly simplified
		if (alpha == 1) {
			res = modulus - ((modulus - 1) / term);
			System.out.format("----: alpha = 1, shortcutting! Result: %d\n", res);
			return res;
		}

		// MI: (OMEGA - alpha) / term

		int mi = (modulus - alpha) / term;

		// NOW: alpha, omega, term, mi

		// X: term / alpha - 1 = (k+alpha*x)/alpha - 1 = k/alpha + x - 1 (! k/alpha < 1 !)

		int x = term / alpha - 1;

		// term = k + alpha * x ==> k = term - alpha * x

		// sigma: k - alpha

		int k = term - alpha * x;

		int sigma = k - alpha;

		System.out.format("----: alpha = %d, mi: %d, x: %d, k: %d, sigma: %d\n", alpha, mi, x, k, sigma);

		res = (modulus * fmi3(sigma, k) + 1 + x * mi) / k;

		System.out.format("Inv(%d, %d) = (%d * %d + 1 + %d * Inv(%d, %d) / %d\n", term, modulus, x, mi, modulus, sigma, k, k);
		System.out.format("EXPECTED: %d, FOUND: %d\n", fmi1(term, modulus), res);

		return res;

	}

	static int fmiclean(int term, int modulus, int mode) {

		// can save gcd check and canonization in recursive calls
		if (modulus == 0 || gcd(term, modulus) != 1) return -1;
		if (term > modulus) term = canonize(term, modulus);

		switch (mode) {
			case 1:
				return fmirec1(term, modulus);
			case 2:
				return fmirec2(term, modulus);
			default:
				throw new IllegalArgumentException("Bad mode specified");
		}

	}

	/**
	 * Base steps for multiplicative inverse recursion. 1, 2, 3, n-3, n-2 and
	 * n-1 are contemplated
	 *
	 * NOTE: 0 is a valid output (??!?!??)
	 *
	 * @param term
	 * @param modulus
	 * @return
	 */
	private static int checkBaseSteps(int term, int modulus) {
		if (term == 1) {
			return 1;
		}
		else if (term == (modulus - 1)) {
			return term;
		}
		else if (term == 2) {
			return (modulus + 1) / 2;
		}
		else if (term == modulus - 2) {
			return (modulus - 1) / 2;
		}
		else if (term == 3) {
			return (modulus * (3 - (modulus % 3)) + 1) / 3;
		}
		else if (term == modulus - 3) {
			return (modulus * (modulus % 3) - 1) / 3;
		}
		else return 0;
	}

	// Simple recursion
	static int fmirec1(int term, int modulus) {

		int res = checkBaseSteps(term, modulus);
		if (res != 0) return res;

		int alpha = modulus % term;

		return (modulus * fmirec1(term - alpha, term) + 1) / term;
	}

	// alpha-based recursion
	static int fmirec2(int term, int modulus) {

		int res = checkBaseSteps(term, modulus);
		if (res != 0) return res;

		int alpha = modulus % term;
		int mi = (modulus - alpha) / term; // Exact div

		// Alpha-base
		if (alpha == 1) return modulus - mi;

		int x = term / alpha - 1; // Integer div
		int k = term - alpha * x;

		return (modulus * fmirec2(k - alpha, k) + 1 + x * mi) / k;
	}

	static void testfmi(int a, int b) {

		System.out.println("\nTESTING INV(" + a + ", " + b + ")");

		int o1 = fmi1(a, b);
		int o2 = fmiclean(a, b, 1);
		int o3 = fmiclean(a, b, 2);
//		int o2 = fmi2(a, b);
//		int o3 = fmi3(a, b);
//		int o3 = o2;

		System.out.format("Inverse of %d mod %d: %d - %d - %d \t %s\n", a, b, o1, o2, o3, (o1 == o2 && o2 == o3) ? "OK" : "ERROR");
	}



	public static void main(String[] args) {

		testfmi(8806, 6735);

		testfmi(79, 97);

		int a = (int) (Math.random() * 10000);
		int b;

		do {
			b = (int) (Math.random() * 10000);
		} while (gcd(a, b) != 1);
		testfmi(a, b);
		System.out.println("\n\n\n");

		int m = 41;
		for (int i = 2; i < m; i++) {
			if (gcd(m, i) != 1) continue;
			testfmi(i, m);
		}

		System.out.println("\n\n\n");

		m = 7;
		for (int i = m + 1; i < m * 2; i++) {
			if (gcd(m, i) != 1) continue;
			testfmi(m, i);
		}

		System.out.println("\n\n\n");
		m = 9;
		for (int i = m + 1; i < m * 2; i++) {
			if (gcd(m, i) != 1) continue;
			testfmi(m, i);
		}

		System.out.println("\n\n\n");
		m = 11;
		for (int i = m + 1; i < m * 2; i++) {
			if (gcd(m, i) != 1) continue;
			testfmi(m, i);
		}

		System.out.println("\n\n\n");
		m = 13;
		for (int i = m + 1; i < m * 2; i++) {
			if (gcd(m, i) != 1) continue;
			testfmi(m, i);
		}

		System.out.println("\n\n\n");
		m = 15;
		for (int i = m + 1; i < m * 2; i++) {
			if (gcd(m, i) != 1) continue;
			testfmi(m, i);
		}

		System.out.println("\n\n\n");
		m = 17;
		for (int i = m + 1; i < m * 2; i++) {
			if (gcd(m, i) != 1) continue;
			testfmi(m, i);
		}

		System.out.println("\n\n\n");
		m = 22;
		for (int i = m + 1; i < m * 2; i++) {
			if (gcd(m, i) != 1) continue;
			testfmi(m, i);
		}


		System.out.println("\n\n\n");
		m = 24;
		for (int i = m + 1; i < m * 2; i++) {
			if (gcd(m, i) != 1) continue;
			testfmi(m, i);
		}

		System.out.println("\n\n\n");
		m = 3;
		for (int i = m + 1; i < 256; i++) {
			if (gcd(i - m, i) != 1) continue;
			testfmi(i - m, i);
		}

	}



}
