@SuppressWarnings("unused")
public class Test {
    static String packageVisibleString = "Hello, sbt!";

    public static void main(String... args) {
        String lines = String.valueOf(packageVisibleString.length());
        int linesCount = Integer.parseInt(lines);
        System.out.println("String [" + packageVisibleString + "] contains " + linesCount + " chars");
    }
}