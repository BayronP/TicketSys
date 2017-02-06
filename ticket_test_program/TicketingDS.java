/**
 * Created by bayron on 2016/12/6.
 */

public class TicketingDS implements TicketingSystem {
    Route[] routes;

    public TicketingDS(int rn, int cn, int sn, int stn) {

        routes = new Route[rn + 1];
        for (int i = 1;i < rn + 1; i++) {
            routes[i] = new Route(stn, cn, sn);
        }
    }

    public int calIntervalNum(int departure, int arrival, int station) {
        int num = 1;
        for (int j = 1; j < arrival - departure; j++) {
            num = (num << 1) | 1;
        }

        num = num << (station - arrival);

        return num;
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        //System.out.println("Buy Ticket!");
        SeatPair[] sp = routes[route].seats;
        int sta = routes[route].station;
        int num = calIntervalNum(departure, arrival, sta);
        boolean flag = false;
        Integer resI = 0;
        for (int i = 0; i < sp.length; i++) {
            Integer val = sp[i].info.getReference();
            Integer ts = sp[i].info.getStamp();
            while(!flag && (val&num) == num) { //区间满足
                //System.out.println("ReBuy Ticket!");
                flag = sp[i].info.compareAndSet(val, val&(~num), ts, ts + 1);
                if (flag) {
                    resI = ts;
                }
                //System.out.println(ts);
                val = sp[i].info.getReference();
                ts = sp[i].info.getStamp();
            }
            if (flag) {
                Ticket res = new Ticket(passenger, route, departure, arrival, sp[i], resI);
                //System.out.println("Buy a ticket \n" + res.toString());
                return res;
            }
        }
        //System.out.println("Finsh Buy Ticket!");
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        //System.out.println("Inquiry Ticket!");
        int sta = routes[route].station;

        int num = calIntervalNum(departure, arrival, sta);

        SeatPair[] sp = routes[route].seats;
        int cnt = 0;
        for (int i = 0; i < sp.length; i++) {
            if ((sp[i].info.getReference() & num) == num) {
                cnt++;
            }
        }
        return cnt;
    }

    @Override
    public int refundTicket(Ticket ticket) {
        //System.out.println("Refund Ticket!");
        int route = ticket.route;
        int coach = ticket.coach;
        int seat = ticket.seat;
        int dep = ticket.departure;
        int arr = ticket.arrival;

        Route rou = routes[route];

        int stn = rou.station;
        int sid = (coach-1) * rou.seat_per_coach + seat-1;
        SeatPair[] sp = rou.seats;
        boolean flag = false;
        Integer ts = sp[sid].info.getStamp();
        Integer val = sp[sid].info.getReference();
        //System.out.println(val);
        int num = calIntervalNum(dep, arr, stn);
        int res = -1;
        while (!flag && (val & num) == 0) {
            flag = sp[sid].info.compareAndSet(val, val|num, ts, ts+1);
            if (flag) {
                res = ts;
            }
            ts = sp[sid].info.getStamp();
            val = sp[sid].info.getReference();
        }
        //if (res != -1) {
            //System.out.println("Refund a ticket \n" + ticket.toString());
        //}
        //System.out.println(sp[sid].info.get());
        return res;
    }
}
