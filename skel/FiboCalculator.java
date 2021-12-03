public class FiboCalculator {
    static int compute(int n) {
        if (n <= 1) {
            return n;
        } else {
            return compute(n - 2) + compute(n - 1);
        }
    }
}