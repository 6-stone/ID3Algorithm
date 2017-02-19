import java.util.List;     //程序中会用到list、set、map三种集合类
import java.util.Set; 
import java.util.Map; 
import java.util.LinkedList;  
import java.util.HashMap;  
import java.util.Map.Entry;  

//主类DecisionTree
public class DecisionTree {  

      //构造readSamples方法用来读取读取数据，返回的是Map集合类，
	 //其中key为已知分类 ，value是 属于该分类的样本的列表      
    static Map<Object, List<Sample>> readSamples(String[] attrNames) {  
    	
         //定义二维数组rawData存储样本数据的属性和分类   
        //每一行前四个元素为属性，每行最后一个元素为样本所属分类  
        Object[][] rawData = new Object[][] {  
                { "<=30  ", "H  ", "I ", "low     ", "bad" },  
                { "<=30  ", "H  ", "I ", "high    ", "good" },  
                { "<=30  ", "H  ", "II ", "medium    ", "bad" }, 
                { "<=30  ", "H  ", "II ", "high    ", "good" },
                { "<=30  ", "L  ", "I ", "high     ", "good" },  
                { "<=30  ", "L  ", "I ", "low    ", "good" },  
                { "<=30  ", "L  ", "II ", "low    ", "good" }, 
                { "<=30  ", "M  ", "I ", "high     ", "good" },  
                { "<=30  ", "M  ", "I ", "medium    ", "good" },  
                { "<=30  ", "M  ", "II ", "medium    ", "good" }, 
                { "<=30  ", "M  ", "I ", "low    ", "good" }, 
                { "31-51 ", "M  ", "I ", "medium     ", "good" }, 
                { "31-51 ", "M  ", "II ", "medium     ", "good" }, 
                { "31-51 ", "M  ", "I ", "low     ", "bad" }, 
                { "31-51 ", "H  ", "I ", "high    ", "good" }, 
                { "31-51 ", "H  ", "I ", "medium     ", "good" }, 
                { "31-51 ", "H  ", "I ", "low    ", "good" }, 
                { "31-51 ", "H  ", "II ", "high     ", "bad" }, 
                { "31-51 ", "H  ", "II ", "low     ", "bad" }, 
                { "31-51 ", "L  ", "I ", "high     ", "good" }, 
                { "31-51 ", "L  ", "I ", "low    ", "good" }, 
                { "31-51 ", "M  ", "II ", "high     ", "bad" }, 
                { "31-51 ", "M  ", "I ", "high     ", "good" }, 
                { ">50  ", "M   ", "I ", "high     ", "bad" },  
                { ">50  ", "M   ", "II", "high     ", "bad" },  
                { ">50  ", "M   ", "I", "medium    ", "good" } 
        };
  
        // 读取样本属性及其所属分类，构造表示样本的Sample对象，并按good和bad二分类划分样本集  
        Map<Object, List<Sample>> whole = new HashMap<Object, List<Sample>>();  
        for (Object[] row : rawData) {           //for each语句遍历 二维数组 
            Sample sample = new Sample();       //定义个Sample类的对象sample
            int i = 0;   
            for (int n = row.length - 1; i < n; i++)  
                sample.setAttribute(attrNames[i], row[i]);  //输入4种属性，存入小HashMap中
            sample.setCategory(row[i]);                     //输入分类值
            List<Sample> samples = whole.get(row[i]);      //samples是一个sample的列表
            if (samples == null) {  
                samples = new LinkedList<Sample>();  
                whole.put(row[i], samples); //将分类值作为key，4种属性作为value，存入大HashMap对象whole中
            }  
            samples.add(sample);  
        }  
  
        return whole;  
    }  
  
    /* 
                 通过ID3算法 构造决策树 
     */  
    static Object generateDecisionTree(  
            Map<Object, List<Sample>> categoryToSamples, String[] attrNames) {  
  
        // 如果只有一个样本，将该样本所属分类作为新样本的分类  
        if (categoryToSamples.size() == 1)  
            return categoryToSamples.keySet().iterator().next();  
   
        /* 调用chooseBestTestAttribute方法根据最高信息增益原则选取测试属性  
                             返回的testAttribute数组中含有3个元素，分别为
                             测试属性名数组的下标，测试属性的信息量，属性值——>（分类——>样本数据列表）的map
         */
        Object[] testAttribute= chooseBestTestAttribute(categoryToSamples, attrNames);  
  
        // 决策树根结点，分支属性为选取的测试属性 来构造决策树
        Tree tree = new Tree(attrNames[(Integer) testAttribute[0]]);  
  
        // 已用过的测试属性不应再次被选为测试属性  
        //用新数组newAttrNames存储没有被选为测试属性的属性名
        String[] newAttrNames = new String[attrNames.length - 1];  
        for (int i = 0, j = 0; i < attrNames.length; i++)  
            if (i != (Integer) testAttribute[0])  
            	newAttrNames[j++] = attrNames[i];  
  
        // 根据分支属性生成分支 ，使用Map.entry遍历生成完整决策树 
        //splits对象为map类型：属性值——>（分类——>样本数据列表）
        @SuppressWarnings("unchecked")  
        Map<Object, Map<Object, List<Sample>>> splits =  
        /* NEW LINE */(Map<Object, Map<Object, List<Sample>>>) testAttribute[2];  
        for (Entry<Object, Map<Object, List<Sample>>> entry : splits.entrySet()) {  
            Object attrValue = entry.getKey();  
            Map<Object, List<Sample>> split = entry.getValue();  
            Object child = generateDecisionTree(split, newAttrNames);  
            tree.setChild(attrValue, child);  
        }  
  
        return tree;  
    }  
  
    /*
                    选取测试属性。根据ID3算法原理，最优是指根据选取的测试属性分支，
                    确定新样本的测试属性获得的信息增益最大 ， 这等价于则从各分支确定新样本 
                    的分类需要的信息量之和最小。
                    返回数组：选取的属性下标、信息量之和、Map(属性值->(分类->样本列表)) 
     */  
    static Object[] chooseBestTestAttribute(  
            Map<Object, List<Sample>> categoryToSamples, String[] attrNames) {  
  
        int minIndex = -1; // 最优属性下标  
        double minValue = Double.MAX_VALUE; // 最小信息量  
        Map<Object, Map<Object, List<Sample>>> minSplits = null; // 最优分支方案  
  
        // 对每一个属性，计算将其作为测试属性的情况下在各分支确定新样本的分类需要的信息量之和，选取最小为最优  
        for (int attrIndex = 0; attrIndex < attrNames.length; attrIndex++) {  
            int allCount = 0;  // 统计样本总数的计数器  
  
            // 按当前属性构建Map：key为属性值->value为(分类->样本列表)  
            Map<Object, Map<Object, List<Sample>>> curSplits =  
            /* NEW LINE */new HashMap<Object, Map<Object, List<Sample>>>();  
            for (Entry<Object, List<Sample>> entry : categoryToSamples  
                    .entrySet()) {  
                Object category = entry.getKey();  
                List<Sample> samples = entry.getValue();  
                //for each 循环遍历26组数据
                for (Sample sample : samples) {  
                    Object attrValue = sample  
                            .getAttribute(attrNames[attrIndex]);  
                    Map<Object, List<Sample>> split = curSplits.get(attrValue);  
                    if (split == null) {  
                        split = new HashMap<Object, List<Sample>>();  
                        curSplits.put(attrValue, split);  
                    }  
                    List<Sample> splitSamples = split.get(category);  
                    if (splitSamples == null) {  
                        splitSamples = new LinkedList<Sample>();  
                        split.put(category, splitSamples);  
                    }  
                    splitSamples.add(sample);  
                }  
                allCount += samples.size();  
            }  
          
            // 计算将当前属性作为测试属性的情况下在各分支确定新样本的分类需要的信息量之和  
            double curValue = 0.0; // 某一属性总的信息
            for (Map<Object, List<Sample>> splits : curSplits.values()) {  
                double perSplitCount = 0;  
                for (List<Sample> list : splits.values())  
                    perSplitCount += list.size(); // 累计当前分支样本数  
                double perSplitValue = 0.0; // 计数器：当前分支  
                
                //利用ID3算法，计算按照每个每个属性划分的信息
                for (List<Sample> list : splits.values()) {  
                    double p = list.size() / perSplitCount;  
                    perSplitValue -= p * (Math.log(p) / Math.log(2));  
                }  
                curValue += (perSplitCount / allCount) * perSplitValue;  
            }  
  
            // ID3算法选取具有最高信息增益的属性作为测试属性，
            //信息增益等于期望信息减去给定样本分类属性对应的信息
            //因此，选取信息增益最大，等价于选取信息最小的作为测试属性
            if (minValue > curValue) {  
                minIndex = attrIndex;  
                minValue = curValue;  
                minSplits = curSplits;  
            }  
        }  
  
        //返回 所采用的测试属性的数组的下标，信息量，一个map(属性值——>(分类——>样本列表））
        return new Object[] { minIndex, minValue, minSplits };  
    }  
  
    /* 
     * 采用递归方式，将决策树输出
     */  
    static void outputDecisionTree(Object obj, int level, Object from) {  
        for (int i = 0; i < level; i++)  
            System.out.print("|-----");  
        if (from != null)  
            System.out.printf("(%s):", from);  
        if (obj instanceof Tree) {  
            Tree tree = (Tree) obj;  
            String attrName = tree.getAttribute();  
            System.out.printf("[%s = ?]\n", attrName); 
            
            //for each 循环遍历某一属性下的所有分支
            for (Object attrValue : tree.getAttributeValues()) {  
            	//通过属性值获取子决策树
                Object child = tree.getChild(attrValue);  
                //递归输出决策树
                outputDecisionTree(child, level + 1, attrName + " = "  
                        + attrValue);  
            }  
        } 
        //当前结点判断不是树时，输出分类结果
        else {  
            System.out.printf("[class = %s]\n", obj);  
        }   
    }  
  
    /*
                 处理样本数据的静态类Sample，提供五个方法         
                  每个样本都是一个HashMap，包含多个属性和一个分类值 
     */  
    static class Sample {  
  
        private Map<String, Object> attributes = new HashMap<String, Object>();  
  
        private Object category;  
       
        //返回一个value,通过key值name从HashMap中获取value,返回一个Object
        public Object getAttribute(String name) {  
            return attributes.get(name);  
        }  
        
        //向HashMap中输入一个键值对
        public void setAttribute(String name, Object value) {  
            attributes.put(name, value);  
        }  
        
        //返回分类值
        public Object getCategory() {  
            return category;  
        }  
  
        //输入分类值
        public void setCategory(Object category) {  
            this.category = category;  
        }  
  
        //将HashMap对象转换为字符串
        public String toString() {  
            return attributes.toString();  
        }  
  
    }  
  
    /*
                 决策树（非叶结点），决策树中的每个非叶结点都引导了一棵决策树 
                 每个非叶结点包含一个分支属性和多个分支，分支属性的每个值对应一个分支，该分支引导了一棵子决策树 
     */  
    static class Tree {  
  
        private String attribute;  
  
        private Map<Object, Object> children = new HashMap<Object, Object>();  
  
      //构造方法，负责初始化
        public Tree(String attribute) {    
            this.attribute = attribute;  
        }  
  
        //获取属性
        public String getAttribute() {     
            return attribute;  
        }  
  
        //通过所给属性的属性值，获取子决策树
        public Object getChild(Object attrValue) {  
            return children.get(attrValue);  
        }  
  
        //将属性值和子决策树放入HashMap中，构成上一级决策树
        public void setChild(Object attrValue, Object child) {  
            children.put(attrValue, child);  
        }  
  
        //返回决策树HashMap的key（属性值）集合
        public Set<Object> getAttributeValues() {  
            return children.keySet();  
        }  
  
    }  
    //主函数
    public static void main(String[] args) {  
    	
    	//讲四种属性存入数组attrNames
        String[] attrNames = new String[] { "age", "education", "area", "level" };  
  
        //利用readSamples读取样本集  
        Map<Object, List<Sample>> samples = readSamples(attrNames);  
  
        //利用generateDecisionTree生成决策树  
        Object decisionTree = generateDecisionTree(samples, attrNames);  
  
        //通过outputDecisionTree输出决策树  
        outputDecisionTree(decisionTree, 0, null);  
    }  
  
}