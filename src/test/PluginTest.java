import org.junit.*;
import static org.junit.Assert.*;
import com.fiveruns.dash.*;


public class PluginTest {
    
    @Test
    public void test_if_metricsGathered() throws Exception {
//        generateGarbage();
        
        Recipe[] recipes = {
            Recipe.getJvmRecipe(),
        };
        Plugin.start("876ab538bd015dd81bd07c5bed60202420596316", recipes);
        Thread.sleep(10000);
        Plugin.stop();
    }
    
    private void generateGarbage() {
        String[] s = new String[100000];
        int count = 0;
        for (int i=0;i<100000;i++) {
            s[i] = new String(i + "abyafdlasdjf");
            count += s[i].length();
        }
        s = null;
        System.out.println(count);
    }
}
