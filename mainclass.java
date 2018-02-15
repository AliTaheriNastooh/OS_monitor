import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ASUS on 1/22/2018.
 */
public class mainclass {
    public static class timer extends Thread {
        private final Lock lock = new ReentrantLock(true);
        private final Condition sleep = lock.newCondition();
        int currentTime = 0;
        int lastTime = 1;
        Random rand = new Random();

        void getSleep(staff s) throws InterruptedException {
            lock.lock();
            try {
                lastTime = lastTime + rand.nextInt(2) + 1;
                s.setTime(lastTime);
                System.out.println(s.name + " now sleep");
                sleep.await();
                if (s.check(currentTime)) {
                    System.out.println(s.name + " wakeup and start to work");
                } else {
                    System.out.println(s.name + " wake up another person");
                    sleep.signal();
                    System.out.println(s.name + " sleep again");
                    sleep.await();
                }
            } finally {
                lock.unlock();
            }
        }

        void setSignal() {
            lock.lock();
            try {
                sleep.signal();
            } finally {
                lock.unlock();
            }
        }

        public void run() {

            while (true) {
                System.out.println("time" + currentTime);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setSignal();
                currentTime++;
            }

        }

    }

    ///////////////////

    public static class staff extends Thread {
        timer clock;
        String name;
        int time_wakeup;

        staff(String me, timer clk) {
            name = me;
            clock = clk;
        }

        void setTime(int t) {
            time_wakeup = t;
            System.out.println(name + "time_wakeup " + time_wakeup);
        }

        boolean check(int t) {
            return t == time_wakeup;
        }

        void work() throws InterruptedException {
            Thread.sleep(4000);
        }

        public void run() {
            while (true) {
                try {
                    work();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    clock.getSleep(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] arg) {
        timer mytimer = new timer();
        mytimer.start();
        staff[] mystaff = new staff[4];
        for (int i = 0; i < 4; i++) {
            mystaff[i] = new staff("staff" + i, mytimer);
            mystaff[i].start();
        }
    }
}
