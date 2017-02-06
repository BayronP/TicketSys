package ticketingsystem;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bayron on 2016/12/14.
 */

class SeatPair {
    int coachId;
    int seatId;
    AtomicInteger info;
    SeatPair(int cId, int sId, int inf) {
        coachId = cId;
        seatId = sId;
        info = new AtomicInteger(inf);
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
    Ticket() {
    }

    Ticket(int rt, int dep, int arr) {
        route = rt;
        departure = dep;
        arrival = arr;
        type = 2;
    }

    Ticket(String pass, int rt, int dep, int arr, SeatPair sp) {
        tid = counter.getAndIncrement();
        passenger = pass;
        route = rt;
        departure = dep;
        arrival = arr;
        coach = sp.coachId;
        seat = sp.seatId;
        type = 1;
    }

    Ticket(Ticket t, int flag) {
        tid = t.tid;
        passenger = t.passenger;
        route = t.route;
        departure = t.departure;
        arrival = t.arrival;
        coach = t.coach;
        seat = t.seat;
        type = 0;
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
    boolean refundTicket(Ticket ticket);
}
