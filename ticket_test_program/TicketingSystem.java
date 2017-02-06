import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Created by bayron on 2016/12/14.
 */

class SeatPair {
    int coachId;
    int seatId;
    AtomicStampedReference<Integer> info;
    SeatPair(int cId, int sId, int inf) {
        coachId = cId;
        seatId = sId;
        info = new AtomicStampedReference<Integer>(inf, 0);
    }
}

class Route {
    SeatPair[] seats;
    int station;
    int coach_per_route;
    int seat_per_coach;

    Route(int stn, int cn, int sn) {
        station = stn;
        coach_per_route = cn;
        seat_per_coach = sn;
        seats = new SeatPair[cn*sn];
        int num = 1;

        for (int j = 1; j < stn - 1; j++) {
            num = (num << 1) | 1;
        }

        for (int i = 0; i < cn; i++) {
            for (int j = 0; j < sn; j++) {
                seats[i*sn + j] = new SeatPair(i+1, j+1, num);
            }
        }
    }
}

class Ticket {
    static AtomicInteger counter = new AtomicInteger(0);

    long tid;
    String passenger;
    int route;
    int coach;
    int seat;
    int departure;
    int arrival;

    // 0 表示退票  1表示成功的购票  2表示失败的购票
    int type;
    // 线性化点的时间戳
    int timeStamp;

    Ticket(int rt, int dep, int arr, int ts) {
        route = rt;
        departure = dep;
        arrival = arr;
        type = 2;
        timeStamp = ts;
    }

    Ticket(String pass, int rt, int dep, int arr, SeatPair sp, int ts) {
        tid = counter.getAndIncrement();
        passenger = pass;
        route = rt;
        departure = dep;
        arrival = arr;
        coach = sp.coachId;
        seat = sp.seatId;
        type = 1;

        timeStamp = ts;
    }

    Ticket(Ticket t, int flag, int ts) {
        tid = t.tid;
        passenger = t.passenger;
        route = t.route;
        departure = t.departure;
        arrival = t.arrival;
        coach = t.coach;
        seat = t.seat;
        type = 0;

        timeStamp = ts;
    }

    public String toString() {
        String res = "Ticket id : " + tid + "\n";
        res += "Ticket rou : " + route + "\n";
        res += "Ticket dep : " + departure + "\n";
        res += "Ticket arr : " + arrival + "\n";
        res += "Ticket coach : " + coach + "\n";
        res += "Ticket seat : " + seat + "\n";

        return res;
    }
}

public interface TicketingSystem {
    Ticket buyTicket(String passenger, int route,
                     int departure, int arrival);
    int inquiry(int route, int departure, int arrival);
    int refundTicket(Ticket ticket);
}
