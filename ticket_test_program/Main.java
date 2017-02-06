import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    static Lock lock = new ReentrantLock();
    static int routenum = 5;
    static int coachnum = 8;
    static int seatnum = 100;
    static int stationnum = 10;
    static TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum);
    static ArrayList<Ticket> history = new ArrayList<Ticket>();
    static Integer[] cnt = new Integer[64];
    //System.out.println("执行时间："+excTime+"s");

    public static void Analyse() {

        // tid 检验
        boolean flag = false;
        HashSet<Long> idset = new HashSet<Long>();
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).type == 1) {
                Long tid = history.get(i).tid;
                if (idset.contains(tid)) {
                    System.out.println("出现重复的tid!");
                    flag = true;
                } else {
                    idset.add(tid);
                }
            }
        }
        if (!flag) {
            System.out.println("Tid重复检测通过！");
        }

        // Test if the process is valid!
        // 是否无票的时候售出座位，座位是否有重复等
        // 单线程replay日志，来验证是否正确。

        boolean test[][][][][] = new boolean[routenum+1][stationnum+1][stationnum+1][coachnum+1][seatnum+1];

        boolean ref_flag = false, buy_flag = false;
        for (int i = 0; i < history.size(); i++) {
            Ticket k = history.get(i);
            int route = k.route;
            int dep = k.departure;
            int arr = k.arrival;
            int coach = k.coach;
            int seat = k.seat;



            if (k.type == 0) { // 退票检验
                if (!test[route][dep][arr][coach][seat]) {
                    System.out.println("退了未购买的座位  " + i);
                    ref_flag = true;
                }
                // 成功退票  区间座位更新
                else {
                    //test[route][dep][arr][coach][seat] = false;
                    for (int j = 1; j < stationnum; j++) {
                        for (int l = j+1; l <= stationnum; l++) {
                            if (l <= dep || j >= arr) {

                            } else {
                                test[route][j][l][coach][seat] = false;
                            }
                        }
                    }
                }
            }
            else if (k.type == 1) {  // 购票检验
                // 判断购买的位置是否有人
                if (test[route][dep][arr][coach][seat]) {
                    System.out.println("购买的座位被重复购买！" + i);
                    buy_flag = true;
                } else {
                    for (int j = 1; j < stationnum; j++) {
                        for (int l = j+1; l <= stationnum; l++) {
                            if (l <= dep || j >= arr) {

                            } else {
                                test[route][j][l][coach][seat] = true;
                            }
                        }
                    }
                }
            }
            /*
            else {     // 购买失败的检验
                // 看看route车次dep到arr是否有座位

                for (int j = 1; j <= coachnum; j++) {
                    for (int l = 1; l <= seatnum; l++) {
                        if (test[route][dep][arr][j][l] == false) {
                            System.out.println("存在座位，但是购票失败！");
                        }
                    }
                }
            }*/

        }
        if (!ref_flag) System.out.println("退票检测通过！");
        if (!buy_flag) System.out.println("购票检测通过！");
    }

    public static class TickThread extends Thread {
        public int op_per_thread;

        public TickThread(int oppt) {
            op_per_thread = oppt;
        }

        public int getOpPerThread() {
            return op_per_thread;
        }

        public void run() {
            int cnt = 0;
            Queue<Ticket> ticketQueue = new LinkedList<Ticket>();
            Random random = new Random();
            for (int i = 0; i < op_per_thread; i++) {
                int rnum = random.nextInt(10); // 0-9
                // 0 - 5 6 - 8 9
                if (rnum <= 8) {
                    int r = random.nextInt(routenum)+1;
                    int d = 0;
                    while (d == 0) d = random.nextInt(stationnum);
                    int a = d + random.nextInt(stationnum - d) + 1;
                    if (rnum <= 5) {
                        int k = tds.inquiry(r, d, a);
                    } else {
                        Ticket t = tds.buyTicket("bayron", r, d, a);
                        if (t == null) {
                            // t = new Ticket(r,d,a);
                        } else {
                            ticketQueue.add(t);
                        }
                    }
                } else {
                    if (ticketQueue.isEmpty()) {
                        cnt += 1;
                        continue;
                    }
                    Ticket t = ticketQueue.poll();
                    tds.refundTicket(t);

                }
            }
            op_per_thread -= cnt;
        }
    }

    public static class ValidThread extends Thread {
        public void run() {
            //System.out.println("in");
            Queue<Ticket> ticketQueue = new LinkedList<Ticket>();
            Random random = new Random();
            long thid = Thread.currentThread().getId();
            for (int i = 0; i < 100000; i++) {
                int rnum = random.nextInt(10); // 0-9
                // 0 - 5 6 - 8 9
                if (rnum <= 8) {
                    int r = random.nextInt(routenum)+1;
                    int d = 0;
                    while (d == 0) d = random.nextInt(stationnum);
                    int a = d + random.nextInt(stationnum - d) + 1;
                    if (rnum <= 5) {
                        int k = tds.inquiry(r, d, a);
                    } else {
                        Ticket t = tds.buyTicket("bayron", r, d, a);

                        // 买票失败   暂不考虑
                        if (t == null) {
                            //t = new Ticket(r,d,a);
                        } else {
                            ticketQueue.add(t);
                        }

                        lock.lock();
                        try {
                            if (t != null) {
                                history.add(t);
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                } else {
                    if (ticketQueue.isEmpty()) continue;
                    Ticket t = ticketQueue.poll();
                    int ts = tds.refundTicket(t);
                    if (ts != -1) {
                        lock.lock();
                        try {
                            history.add(new Ticket(t, 0, ts)); //0 表示废票
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            }
            //System.out.println("Done!");
        }
    }

    public static void main(String[] args) throws IOException {
        /*File file = new File("/Users/bayron/Desktop/log.txt");

        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        */
        int arr[] = {4,8,16,32,64};
        for (int j = 0; j < arr.length; j++) {
            int threadnum = arr[j];
            int op_per_thread = 100000;

            System.out.println(threadnum + "个线程，每个线程执行100000次操作！");
            ValidThread a[] = new ValidThread[threadnum];
            for (int i = 0; i < threadnum; i++) {
                a[i] = new ValidThread();
            }
            long startTime = System.currentTimeMillis();  //计时开始
            for (int i = 0; i < threadnum; i++) {
                a[i].start();
            }

            try {
                //double sum = 0.0;
                for (ValidThread my : a) {
                    my.join();
                    //sum += my.getOpPerThread();
                }

                long endTime = System.currentTimeMillis();
                float excTime = (float) (endTime - startTime) / 1000; // 结束时间
                //System.out.println("线程数为: " + threadnum);
                //System.out.println("有效执行操作数: " + (int)sum);
                System.out.println("执行时间: " + excTime + "s");
                //double k = sum / excTime;
                //System.out.println("吞吐率为: " + (int)k + " ops" + "\n");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // System.out.println("开始验证！");

            Collections.sort(history, new Comparator<Ticket>() {
                @Override
                public int compare(Ticket o1, Ticket o2) {
                    //System.out.println("hah");
                    int route_a = o1.route;
                    int route_b = o2.route;
                    int coach_a = o1.coach;
                    int coach_b = o2.coach;
                    int seat_a = o1.seat;
                    int seat_b = o2.seat;
                    int ts_a = o1.timeStamp;
                    int ts_b = o2.timeStamp;

                    if (route_a > route_b) return 1;
                    else if (route_a < route_b) return -1;
                    else {
                        if (coach_a > coach_b) return 1;
                        else if (coach_a < coach_b) return -1;
                        else {
                            if (seat_a > seat_b) return 1;
                            else if (seat_a < seat_b) return -1;
                            else {
                                if (ts_a > ts_b) return 1;
                                else if (ts_a < ts_b) return -1;
                                else return 0;
                            }
                        }
                    }
                }
            });
            /*String ab = "";
            for (int i = 0; i < history.size(); i++) {
                Ticket tic =  history.get(i);
                ab += tic.type + " " + tic.route + " " + tic.departure + " " +
                        tic.arrival + " " + tic.coach + " " + tic.seat + " " + tic.timeStamp + "\n";
            }
            fw.write(ab);
            fw.close();
            */
            // history 分析
            Analyse();   //验证分析
        }
    }
}
