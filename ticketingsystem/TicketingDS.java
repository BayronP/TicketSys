package ticketingsystem;
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
        SeatPair[] sp = routes[route].seats;
        int sta = routes[route].station;
        //System.out.println(sta);
        int num = calIntervalNum(departure, arrival, sta);
        //System.out.println("calIntervalNum");
        //System.out.println(num);
        boolean flag = false;
        //System.out.println("sp size : " + sp.length);
        for (int i = 0; i < sp.length; i++) {
            int val = sp[i].info.get();
            //System.out.println(sp[i].coachId + " " +sp[i].seatId);
            while(!flag && (val&num) == num) { //区间满足
                //System.out.println("Round_BUY!");
                flag = sp[i].info.compareAndSet(val, val&(~num));
                val = sp[i].info.get();
                //System.out.println(val);
                //System.out.println(sp[i].info.get());
            }
            if (flag) {
                Ticket res = new Ticket(passenger, route, departure, arrival, sp[i]);
                //System.out.println("Buy a ticket \n" + res.toString());
                return res;
            }
        }
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        int sta = routes[route].station;

        int num = calIntervalNum(departure, arrival, sta);

        SeatPair[] sp = routes[route].seats;
        int cnt = 0;
        for (int i = 0; i < sp.length; i++) {
            if ((sp[i].info.get() & num) == num) {
                cnt++;
            }
        }
        return cnt;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
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
        int val = sp[sid].info.get();
        //System.out.println(val);
        int num = calIntervalNum(dep, arr, stn);
        while (!flag && (val & num) == 0) {
            //System.out.println("Round_REFUND!");
            flag = sp[sid].info.compareAndSet(val, val|num);
            val = sp[sid].info.get();
        }
        if (flag) {
            //System.out.println("Refund a ticket \n" + ticket.toString());
        }
        //System.out.println(sp[sid].info.get());
        return flag;
    }
}
