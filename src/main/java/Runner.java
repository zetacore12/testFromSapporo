public class Runner {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("使い方: java Runner <クラス名> <メソッド名>");
            return;
        }

        String className = args[0];
        String methodName = args[1];

        try {
            // クラスをロード
            Class<?> clazz = Class.forName(className);

            // メソッド取得（引数なし想定）
            var method = clazz.getDeclaredMethod(methodName);

            // staticかどうか判定
            Object instance = null;
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                instance = clazz.getDeclaredConstructor().newInstance();
            }

            // 実行
            method.invoke(instance);

        } catch (ClassNotFoundException e) {
            System.out.println("クラスが見つかりません: " + className);
        } catch (NoSuchMethodException e) {
            System.out.println("メソッドが見つかりません: " + methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
