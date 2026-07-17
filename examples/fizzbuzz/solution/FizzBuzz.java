package net.unit8.kysymys.example.fizzbuzz;

/**
 * 参考解答。verify.sh はこれをスタブに上書きして「解けた状態」の提出を再現する。
 * 研修で配るときはこの solution/ ディレクトリを外してよい。
 */
public class FizzBuzz {

    public String convert(int n) {
        boolean fizz = n % 3 == 0;
        boolean buzz = n % 5 == 0;
        if (fizz && buzz) {
            return "FizzBuzz";
        }
        if (fizz) {
            return "Fizz";
        }
        if (buzz) {
            return "Buzz";
        }
        return Integer.toString(n);
    }
}
