package ColissionSystem;

import java.awt.*;

public class CollisionSystem {
    private MinPQ<Event> pq;        // the priority queue
    private double t  = 0.0;        // simulation clock time
    private double hz = 0.5;        // number of redraw events per clock tick
    private Particle[] particles;   // the array of particles
    private int players;
    private boolean firstPlayer = true;
    private boolean secondPlayer = true;
    private boolean thirdPlayer = true;
    private boolean fourthPlayer = true;

    public CollisionSystem(Particle[] particles, int players) {
        this.players = players;
        this.particles = particles;
    }

    public void simulate(double limit) {

        // initialize PQ with collision events and redraw event
        pq = new MinPQ<Event>();
        for (int i = 0; i < particles.length; i++) {
            predict(particles[i], limit);
        }
        pq.insert(new Event(0, null, null));        // redraw event


        // the main event-driven simulation loop
        while (!pq.isEmpty()) {

            if(!particles[particles.length - players].king){
                if (firstPlayer){
                    Color newColor = particles[particles.length - players].color;
                    for (int i = 0; i < particles.length; i++) {
                        if(particles[i].color == Color.blue)
                            particles[i].color = newColor;
                    }
                    firstPlayer = false;
                }
            }

            if(!particles[particles.length - (players - 1)].king){
                if (secondPlayer){
                    Color newColor = particles[particles.length - (players - 1)].color;
                    for (int i = 0; i < particles.length; i++) {
                        if(particles[i].color == Color.red)
                            particles[i].color = newColor;
                    }
                    secondPlayer = false;
                }
            }

            if(players >= 30) {
                if (!particles[particles.length - (players - 2)].king) {
                    if (thirdPlayer) {
                        Color newColor = particles[particles.length - (players - 2)].color;
                        for (int i = 0; i < particles.length; i++) {
                            if (particles[i].color == Color.green)
                                particles[i].color = newColor;
                        }
                        thirdPlayer = false;
                    }
                }

                if(players == 4) {
                    if (!particles[particles.length - (players - 3)].king) {
                        if (fourthPlayer) {
                            Color newColor = particles[particles.length - (players - 3)].color;
                            for (int i = 0; i < particles.length; i++) {
                                if (particles[i].color == Color.yellow)
                                    particles[i].color = newColor;
                            }
                            fourthPlayer = false;
                        }
                    }
                }
            }

            // get impending event, discard if invalidated
            Event e = pq.delMin();
            if (!e.isValid()) continue;
            Particle a = e.a;
            Particle b = e.b;

            // physical collision, so update positions, and then simulation clock
            for (int i = 0; i < particles.length; i++)
                particles[i].move(e.time - t);
            t = e.time;

            // process event
            if      (a != null && b != null) a.bounceOff(b);              // particle-particle collision
            else if (a != null && b == null) a.bounceOffVerticalWall();   // particle-wall collision
            else if (a == null && b != null) b.bounceOffHorizontalWall(); // particle-wall collision
            else if (a == null && b == null) redraw(limit);               // redraw event

            // update the priority queue with new collisions involving a or b
            predict(a, limit);
            predict(b, limit);
        }
    }

    private void predict(Particle a, double limit) {
        if (a == null) return;

        // particle-particle collisions
        for (int i = 0; i < particles.length; i++) {
            double dt = a.timeToHit(particles[i]);
            if (t + dt <= limit)
                pq.insert(new Event(t + dt, a, particles[i]));
        }

        // particle-wall collisions
        double dtX = a.timeToHitVerticalWall();
        double dtY = a.timeToHitHorizontalWall();
        if (t + dtX <= limit) pq.insert(new Event(t + dtX, a, null));
        if (t + dtY <= limit) pq.insert(new Event(t + dtY, null, a));
    }

    // redraw all particles
    private void redraw(double limit) {
        StdDraw.clear();

        for (int i = 0; i < particles.length; i++) {
            particles[i].draw();
        }
        StdDraw.show(20);
        if (t < limit) {
            pq.insert(new Event(t + 1.0 / hz, null, null));
        }
    }

    private static class Event implements Comparable<Event> {
        private final double time;         // time that event is scheduled to occur
        private final Particle a, b;       // particles involved in event, possibly null
        private final int countA, countB;  // collision counts at event creation


        // create a new event to occur at time t involving a and b
        public Event(double t, Particle a, Particle b) {
            this.time = t;
            this.a    = a;
            this.b    = b;
            if (a != null) countA = a.count();
            else           countA = -1;
            if (b != null) countB = b.count();
            else           countB = -1;
        }

        // compare times when two events will occur
        public int compareTo(Event that) {
            if      (this.time < that.time) return -1;
            else if (this.time > that.time) return +1;
            else                            return  0;
        }

        // has any collision occurred between when event was created and now?
        public boolean isValid() {
            if (a != null && a.count() != countA) return false;
            if (b != null && b.count() != countB) return false;
            return true;
        }

    }

    public static void main(String[] args) {
        StdDraw.setXscale(1.0/22.0, 21.0/22.0);
        StdDraw.setYscale(1.0/22.0, 21.0/22.0);
        StdDraw.show(0);
        Particle[] particles;

        int N = 100; //# of particles
        int P = 4; // # of players
        particles = new Particle[(int) (N*1.2) + P];
        for (int i = 0; i < N; i++) particles[i] = new Particle((i%P));

        int obstacles = (int) (N * 1.2);
        for (int i = N; i < obstacles; i++) particles[i] = new Particle((-1));

        if(P >= 2) {
            particles[particles.length - P] = new Particle(0, 0, 0.02, 0.02, 0.02, 0.5, Color.blue);
            particles[particles.length - (P- 1)] = new Particle(1, 1, -0.02, -0.02, 0.02, 0.5, Color.red);
        }
        if(P >= 3) {
            particles[particles.length - (P - 2)] = new Particle(1, 0, 0.02, 0.02, 0.02, 0.5, Color.green);
            if(P == 4){
                particles[particles.length - (P - 3)] = new Particle(0, 1, 0.02, 0.02, 0.02, 0.5, Color.yellow);
            }
        }

        CollisionSystem system = new CollisionSystem(particles, P);
        system.simulate(10000);
    }
}