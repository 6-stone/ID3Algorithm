import java.util.List;     //�����л��õ�list��set��map���ּ�����
import java.util.Set; 
import java.util.Map; 
import java.util.LinkedList;  
import java.util.HashMap;  
import java.util.Map.Entry;  

//����DecisionTree
public class DecisionTree {  

      //����readSamples����������ȡ��ȡ���ݣ����ص���Map�����࣬
	 //����keyΪ��֪���� ��value�� ���ڸ÷�����������б�      
    static Map<Object, List<Sample>> readSamples(String[] attrNames) {  
    	
         //�����ά����rawData�洢�������ݵ����Ժͷ���   
        //ÿһ��ǰ�ĸ�Ԫ��Ϊ���ԣ�ÿ�����һ��Ԫ��Ϊ������������  
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
  
        // ��ȡ�������Լ����������࣬�����ʾ������Sample���󣬲���good��bad�����໮��������  
        Map<Object, List<Sample>> whole = new HashMap<Object, List<Sample>>();  
        for (Object[] row : rawData) {           //for each������ ��ά���� 
            Sample sample = new Sample();       //�����Sample��Ķ���sample
            int i = 0;   
            for (int n = row.length - 1; i < n; i++)  
                sample.setAttribute(attrNames[i], row[i]);  //����4�����ԣ�����СHashMap��
            sample.setCategory(row[i]);                     //�������ֵ
            List<Sample> samples = whole.get(row[i]);      //samples��һ��sample���б�
            if (samples == null) {  
                samples = new LinkedList<Sample>();  
                whole.put(row[i], samples); //������ֵ��Ϊkey��4��������Ϊvalue�������HashMap����whole��
            }  
            samples.add(sample);  
        }  
  
        return whole;  
    }  
  
    /* 
                 ͨ��ID3�㷨 ��������� 
     */  
    static Object generateDecisionTree(  
            Map<Object, List<Sample>> categoryToSamples, String[] attrNames) {  
  
        // ���ֻ��һ��������������������������Ϊ�������ķ���  
        if (categoryToSamples.size() == 1)  
            return categoryToSamples.keySet().iterator().next();  
   
        /* ����chooseBestTestAttribute�������������Ϣ����ԭ��ѡȡ��������  
                             ���ص�testAttribute�����к���3��Ԫ�أ��ֱ�Ϊ
                             ����������������±꣬�������Ե���Ϣ��������ֵ����>�����ࡪ��>���������б���map
         */
        Object[] testAttribute= chooseBestTestAttribute(categoryToSamples, attrNames);  
  
        // ����������㣬��֧����Ϊѡȡ�Ĳ������� �����������
        Tree tree = new Tree(attrNames[(Integer) testAttribute[0]]);  
  
        // ���ù��Ĳ������Բ�Ӧ�ٴα�ѡΪ��������  
        //��������newAttrNames�洢û�б�ѡΪ�������Ե�������
        String[] newAttrNames = new String[attrNames.length - 1];  
        for (int i = 0, j = 0; i < attrNames.length; i++)  
            if (i != (Integer) testAttribute[0])  
            	newAttrNames[j++] = attrNames[i];  
  
        // ���ݷ�֧�������ɷ�֧ ��ʹ��Map.entry������������������ 
        //splits����Ϊmap���ͣ�����ֵ����>�����ࡪ��>���������б�
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
                    ѡȡ�������ԡ�����ID3�㷨ԭ��������ָ����ѡȡ�Ĳ������Է�֧��
                    ȷ���������Ĳ������Ի�õ���Ϣ������� �� ��ȼ�����Ӹ���֧ȷ�������� 
                    �ķ�����Ҫ����Ϣ��֮����С��
                    �������飺ѡȡ�������±ꡢ��Ϣ��֮�͡�Map(����ֵ->(����->�����б�)) 
     */  
    static Object[] chooseBestTestAttribute(  
            Map<Object, List<Sample>> categoryToSamples, String[] attrNames) {  
  
        int minIndex = -1; // ���������±�  
        double minValue = Double.MAX_VALUE; // ��С��Ϣ��  
        Map<Object, Map<Object, List<Sample>>> minSplits = null; // ���ŷ�֧����  
  
        // ��ÿһ�����ԣ����㽫����Ϊ�������Ե�������ڸ���֧ȷ���������ķ�����Ҫ����Ϣ��֮�ͣ�ѡȡ��СΪ����  
        for (int attrIndex = 0; attrIndex < attrNames.length; attrIndex++) {  
            int allCount = 0;  // ͳ�����������ļ�����  
  
            // ����ǰ���Թ���Map��keyΪ����ֵ->valueΪ(����->�����б�)  
            Map<Object, Map<Object, List<Sample>>> curSplits =  
            /* NEW LINE */new HashMap<Object, Map<Object, List<Sample>>>();  
            for (Entry<Object, List<Sample>> entry : categoryToSamples  
                    .entrySet()) {  
                Object category = entry.getKey();  
                List<Sample> samples = entry.getValue();  
                //for each ѭ������26������
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
          
            // ���㽫��ǰ������Ϊ�������Ե�������ڸ���֧ȷ���������ķ�����Ҫ����Ϣ��֮��  
            double curValue = 0.0; // ĳһ�����ܵ���Ϣ
            for (Map<Object, List<Sample>> splits : curSplits.values()) {  
                double perSplitCount = 0;  
                for (List<Sample> list : splits.values())  
                    perSplitCount += list.size(); // �ۼƵ�ǰ��֧������  
                double perSplitValue = 0.0; // ����������ǰ��֧  
                
                //����ID3�㷨�����㰴��ÿ��ÿ�����Ի��ֵ���Ϣ
                for (List<Sample> list : splits.values()) {  
                    double p = list.size() / perSplitCount;  
                    perSplitValue -= p * (Math.log(p) / Math.log(2));  
                }  
                curValue += (perSplitCount / allCount) * perSplitValue;  
            }  
  
            // ID3�㷨ѡȡ���������Ϣ�����������Ϊ�������ԣ�
            //��Ϣ�������������Ϣ��ȥ���������������Զ�Ӧ����Ϣ
            //��ˣ�ѡȡ��Ϣ������󣬵ȼ���ѡȡ��Ϣ��С����Ϊ��������
            if (minValue > curValue) {  
                minIndex = attrIndex;  
                minValue = curValue;  
                minSplits = curSplits;  
            }  
        }  
  
        //���� �����õĲ������Ե�������±꣬��Ϣ����һ��map(����ֵ����>(���ࡪ��>�����б���
        return new Object[] { minIndex, minValue, minSplits };  
    }  
  
    /* 
     * ���õݹ鷽ʽ�������������
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
            
            //for each ѭ������ĳһ�����µ����з�֧
            for (Object attrValue : tree.getAttributeValues()) {  
            	//ͨ������ֵ��ȡ�Ӿ�����
                Object child = tree.getChild(attrValue);  
                //�ݹ����������
                outputDecisionTree(child, level + 1, attrName + " = "  
                        + attrValue);  
            }  
        } 
        //��ǰ����жϲ�����ʱ�����������
        else {  
            System.out.printf("[class = %s]\n", obj);  
        }   
    }  
  
    /*
                 �����������ݵľ�̬��Sample���ṩ�������         
                  ÿ����������һ��HashMap������������Ժ�һ������ֵ 
     */  
    static class Sample {  
  
        private Map<String, Object> attributes = new HashMap<String, Object>();  
  
        private Object category;  
       
        //����һ��value,ͨ��keyֵname��HashMap�л�ȡvalue,����һ��Object
        public Object getAttribute(String name) {  
            return attributes.get(name);  
        }  
        
        //��HashMap������һ����ֵ��
        public void setAttribute(String name, Object value) {  
            attributes.put(name, value);  
        }  
        
        //���ط���ֵ
        public Object getCategory() {  
            return category;  
        }  
  
        //�������ֵ
        public void setCategory(Object category) {  
            this.category = category;  
        }  
  
        //��HashMap����ת��Ϊ�ַ���
        public String toString() {  
            return attributes.toString();  
        }  
  
    }  
  
    /*
                 ����������Ҷ��㣩���������е�ÿ����Ҷ��㶼������һ�þ����� 
                 ÿ����Ҷ������һ����֧���ԺͶ����֧����֧���Ե�ÿ��ֵ��Ӧһ����֧���÷�֧������һ���Ӿ����� 
     */  
    static class Tree {  
  
        private String attribute;  
  
        private Map<Object, Object> children = new HashMap<Object, Object>();  
  
      //���췽���������ʼ��
        public Tree(String attribute) {    
            this.attribute = attribute;  
        }  
  
        //��ȡ����
        public String getAttribute() {     
            return attribute;  
        }  
  
        //ͨ���������Ե�����ֵ����ȡ�Ӿ�����
        public Object getChild(Object attrValue) {  
            return children.get(attrValue);  
        }  
  
        //������ֵ���Ӿ���������HashMap�У�������һ��������
        public void setChild(Object attrValue, Object child) {  
            children.put(attrValue, child);  
        }  
  
        //���ؾ�����HashMap��key������ֵ������
        public Set<Object> getAttributeValues() {  
            return children.keySet();  
        }  
  
    }  
    //������
    public static void main(String[] args) {  
    	
    	//���������Դ�������attrNames
        String[] attrNames = new String[] { "age", "education", "area", "level" };  
  
        //����readSamples��ȡ������  
        Map<Object, List<Sample>> samples = readSamples(attrNames);  
  
        //����generateDecisionTree���ɾ�����  
        Object decisionTree = generateDecisionTree(samples, attrNames);  
  
        //ͨ��outputDecisionTree���������  
        outputDecisionTree(decisionTree, 0, null);  
    }  
  
}