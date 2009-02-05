import com.fiveruns.dash.*;

/**
 * Test Dash client which just starts up and sends basic VM data.
 */
public class Client {
    public static void main(String[] args) {
        Recipe[] recipes = {
            Recipe.getJvmRecipe(),
        };

        // a5aa9f6849cc75e0678230336c8af88f95c96c53 staging
        // 876ab538bd015dd81bd07c5bed60202420596316 production
        Plugin.start("876ab538bd015dd81bd07c5bed60202420596316", recipes);

        try {
            while (true) {
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException ie) {
            System.out.println("Interrupted");
        }
        Plugin.stop();
    }    
}
