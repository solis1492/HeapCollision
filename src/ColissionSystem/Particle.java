package ColissionSystem;

import java.awt.Color;

public class Particle {
    private static final double INFINITY = Double.POSITIVE_INFINITY;

    public boolean king = true;     // player ball
    private double rx, ry;    // position
    private double vx, vy;    // velocity
    private double radius;    // radius
    private double mass;      // mass
    public Color color;      // color
    private int count;        // number of collisions


    public Particle(double rx, double ry, double vx, double vy, double radius, double mass, Color color) {
        this.vx = vx;
        this.vy = vy;
        this.rx = rx;
        this.ry = ry;
        this.radius = radius;
        this.mass   = mass;
        this.color  = color;
        this.king = true;
    }

    public Particle(int colorSelect) {
        rx     = Math.random();
        ry     = Math.random();
        vx     = 0.01 * (Math.random() - 0.5);
        vy     = 0.01 * (Math.random() - 0.5);
        radius = 0.01;
        mass   = 0.5;
        king = false;
        if(colorSelect == -1){
            color = Color.black;
            mass = 50;
            vx = 0;
            vy = 0;
        }
        else if (colorSelect == 0) {
            color = Color.red;
        }
        else if(colorSelect == 1) {
            color = Color.blue;
        }
        else if (colorSelect == 2) {
            color = Color.green;
        }
        else{
            color = Color.yellow;
        }
    }

    public void move(double dt) {
        rx += vx * dt;
        ry += vy * dt;
    }

    public void draw() {
        StdDraw.setPenColor(color);
        StdDraw.filledCircle(rx, ry, radius);
    }

    public int count() { return count; }


    public double timeToHit(Particle b) {
        Particle a = this;
        if (a == b) return INFINITY;
        double dx  = b.rx - a.rx;
        double dy  = b.ry - a.ry;
        double dvx = b.vx - a.vx;
        double dvy = b.vy - a.vy;
        double dvdr = dx*dvx + dy*dvy;
        if (dvdr > 0) return INFINITY;
        double dvdv = dvx*dvx + dvy*dvy;
        double drdr = dx*dx + dy*dy;
        double sigma = a.radius + b.radius;
        double d = (dvdr*dvdr) - dvdv * (drdr - sigma*sigma);
        if (d < 0) return INFINITY;
        return -(dvdr + Math.sqrt(d)) / dvdv;
    }

    public double timeToHitVerticalWall() {
        if      (vx > 0) return (1.0 - rx - radius) / vx;
        else if (vx < 0) return (radius - rx) / vx;
        else             return INFINITY;
    }

    public double timeToHitHorizontalWall() {
        if      (vy > 0) return (1.0 - ry - radius) / vy;
        else if (vy < 0) return (radius - ry) / vy;
        else             return INFINITY;
    }

    public void bounceOff(Particle that) {
        double dx  = that.rx - this.rx;
        double dy  = that.ry - this.ry;
        double dvx = that.vx - this.vx;
        double dvy = that.vy - this.vy;
        double dvdr = dx*dvx + dy*dvy;             // dv dot dr
        double dist = this.radius + that.radius;   // distance between particle centers at collison

        double F = 2 * this.mass * that.mass * dvdr / ((this.mass + that.mass) * dist);
        double fx = F * dx / dist;
        double fy = F * dy / dist;

        this.vx += fx / this.mass;
        this.vy += fy / this.mass;
        that.vx -= fx / that.mass;
        that.vy -= fy / that.mass;

        this.count++;
        that.count++;

        if(this.color == that.color) {
            this.radius = (this.radius <= 0.02 ? this.radius * 1.01 : 0.019);
            //this.mass *= 1.0001;
            that.radius = (that.radius <= 0.02 ? that.radius * 1.01 : 0.019);
            //that.mass *= 1.0001;
        }
        else if (this.color == Color.black){
            this.vx = 0;
            this.vy = 0;
        }
        else if (that.color == Color.black){
            that.vx = 0;
            that.vy = 0;
        }
        else{
            if(this.radius > that.radius) {
                that.color = this.color;
                if(that.king)
                    that.king = !that.king;
            }
            else if (this.radius < that.radius) {
                this.color = that.color;
                if(this.king)
                    this.king = !this.king;
            }
        }
    }

    public void bounceOffVerticalWall() {
        vx = -vx;
        count++;
    }

    public void bounceOffHorizontalWall() {
        vy = -vy;
        count++;
    }

    public double kineticEnergy() { return 0.5 * mass * (vx*vx + vy*vy); }
}