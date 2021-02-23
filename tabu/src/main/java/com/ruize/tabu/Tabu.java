package com.ruize.tabu;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class Tabu {

    private int MAX_GEN;// 迭代次数
    private int N;// 每次搜索邻居个数
    private int ll;// 禁忌长度
    private int cityNum; // 城市数量，编码长度

    private int[][] distance; // 距离矩阵
    private int bestT;// 最佳出现代数

    private int[] Ghh;// 初始路径编码
    private int[] bestGh;// 最好的路径编码
    private int bestEvaluation;
    private int[] LocalGhh;// 当代最好编码
    private int localEvaluation;
    private int[] tempGhh;// 存放临时编码
    private int tempEvaluation;

    private int[][] jinji;// 禁忌表

    private int t;// 当前代数

    private Random random;

    public Tabu() {

    }

    /**
     * constructor of GA
     *
     * @param n
     *            城市数量
     * @param g
     *            运行代数
     * @param c
     *            每次搜索邻居个数
     * @param m
     *            禁忌长度
     *
     **/
    public Tabu(int n, int g, int c, int m) {
        cityNum = n;
        MAX_GEN = g;
        N = c;
        ll = m;
    }

    // 给编译器一条指令，告诉它对被批注的代码元素内部的某些警告保持静默
    @SuppressWarnings("resource")
    /**
     * 初始化Tabu算法类
     * @param filename 数据文件名，该文件存储所有城市节点坐标数据
     * @throws IOException
     */
    public void init(String filename) throws IOException {
        // 读取数据
        int[] x;
        int[] y;
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename)));
        distance = new int[cityNum][cityNum];
        x = new int[cityNum];
        y = new int[cityNum];
        for (int i = 0; i < cityNum; i++) {
            // 读取一行数据，数据格式 1 6734 1453
            strbuff = data.readLine();
            // 字符分割
            String[] strcol = strbuff.split(" ");
            x[i] = Integer.valueOf(strcol[1]);// x坐标
            y[i] = Integer.valueOf(strcol[2]);// y坐标
        }
        // 计算距离矩阵
        // ，针对具体问题，距离计算方法也不一样，此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628
        for (int i = 0; i < cityNum - 1; i++) {
            distance[i][i] = 0; // 对角线为0
            for (int j = i + 1; j < cityNum; j++) {
                double rij = Math
                        .sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j])
                                * (y[i] - y[j])) / 10.0);
                // 四舍五入，取整
                int tij = (int) Math.round(rij);
                if (tij < rij) {
                    distance[i][j] = tij + 1;
                    distance[j][i] = distance[i][j];
                } else {
                    distance[i][j] = tij;
                    distance[j][i] = distance[i][j];
                }
            }
        }
        distance[cityNum - 1][cityNum - 1] = 0;

        Ghh = new int[cityNum];
        bestGh = new int[cityNum];
        bestEvaluation = Integer.MAX_VALUE;
        LocalGhh = new int[cityNum];
        localEvaluation = Integer.MAX_VALUE;
        tempGhh = new int[cityNum];
        tempEvaluation = Integer.MAX_VALUE;

        jinji = new int[ll][cityNum];
        bestT = 0;
        t = 0;

        random = new Random(System.currentTimeMillis());
        /*
         * for(int i=0;i<cityNum;i++) { for(int j=0;j<cityNum;j++) {
         * System.out.print(distance[i][j]+","); } System.out.println(); }
         */

    }

    // 初始化编码Ghh
    void initGroup() {
        int i, j;
        Ghh[0] = random.nextInt(65535) % cityNum;
        for (i = 1; i < cityNum;)// 编码长度
        {
            Ghh[i] = random.nextInt(65535) % cityNum;
            for (j = 0; j < i; j++) {
                if (Ghh[i] == Ghh[j]) {
                    break;
                }
            }
            if (j == i) {
                i++;
            }
        }
    }

    // 复制编码体，复制编码Gha到Ghb
    public void copyGh(int[] Gha, int[] Ghb) {
        for (int i = 0; i < cityNum; i++) {
            Ghb[i] = Gha[i];
        }
    }

    public int evaluate(int[] chr) {
        // 0123
        int len = 0;
        // 编码，起始城市,城市1,城市2...城市n
        for (int i = 1; i < cityNum; i++) {
            len += distance[chr[i - 1]][chr[i]];
        }
        // 城市n,起始城市
        len += distance[chr[cityNum - 1]][chr[0]];
        return len;
    }

    // 邻域交换，得到邻居
    public void Linju(int[] Gh, int[] tempGh) {
        int i, temp;
        int ran1, ran2;

        for (i = 0; i < cityNum; i++) {
            tempGh[i] = Gh[i];
        }
        ran1 = random.nextInt(65535) % cityNum;
        ran2 = random.nextInt(65535) % cityNum;
        while (ran1 == ran2) {
            ran2 = random.nextInt(65535) % cityNum;
        }
        temp = tempGh[ran1];
        tempGh[ran1] = tempGh[ran2];
        tempGh[ran2] = temp;
    }

    // 判断编码是否在禁忌表中
    public int panduan(int[] tempGh) {
        int i, j;
        int flag = 0;
        for (i = 0; i < ll; i++) {
            flag = 0;
            for (j = 0; j < cityNum; j++) {
                if (tempGh[j] != jinji[i][j]) {
                    flag = 1;// 不相同
                    break;
                }
            }
            if (flag == 0)// 相同，返回存在相同
            {
                // return 1;
                break;
            }
        }
        if (i == ll)// 不等
        {
            return 0;// 不存在
        } else {
            return 1;// 存在
        }
    }

    // 解禁忌与加入禁忌
    public void jiejinji(int[] tempGh) {
        int i, j, k;
        // 删除禁忌表第一个编码，后面编码往前挪动
        for (i = 0; i < ll - 1; i++) {
            for (j = 0; j < cityNum; j++) {
                jinji[i][j] = jinji[i + 1][j];
            }
        }

        // 新的编码加入禁忌表
        for (k = 0; k < cityNum; k++) {
            jinji[ll - 1][k] = tempGh[k];
        }

    }

    public void solve() {
        int nn;
        // 初始化编码Ghh
        initGroup();
        copyGh(Ghh, bestGh);// 复制当前编码Ghh到最好编码bestGh
        bestEvaluation = evaluate(Ghh);

        while (t < MAX_GEN) {
            nn = 0;
            localEvaluation = Integer.MAX_VALUE;
            while (nn < N) {
                Linju(Ghh, tempGhh);// 得到当前编码Ghh的邻域编码tempGhh
                if (panduan(tempGhh) == 0)// 判断编码是否在禁忌表中
                {
                    // 不在
                    tempEvaluation = evaluate(tempGhh);
                    if (tempEvaluation < localEvaluation) {
                        copyGh(tempGhh, LocalGhh);
                        localEvaluation = tempEvaluation;
                    }
                    nn++;
                }
            }
            if (localEvaluation < bestEvaluation) {
                bestT = t;
                copyGh(LocalGhh, bestGh);
                bestEvaluation = localEvaluation;
            }
            copyGh(LocalGhh, Ghh);

            // 解禁忌表，LocalGhh加入禁忌表
            jiejinji(LocalGhh);
            t++;
        }

        System.out.println("最佳长度出现代数：");
        System.out.println(bestT);
        System.out.println("最佳长度");
        System.out.println(bestEvaluation);
        System.out.println("最佳路径：");
        for (int i = 0; i < cityNum; i++) {
            System.out.print(bestGh[i] + ",");
        }
    }

    /**
     * @param args
     * @throws IOException
     */

}