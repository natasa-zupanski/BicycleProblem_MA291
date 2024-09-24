import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

class Main {
    Integer over = null;
    double speed_step = 5;

    protected class Constants {
        final double gravity = 9.81;
        final double earth_radius = 6378137;
        final double kinematic_viscosity_of_air = 0.000015672;
        final double drag_coeff_rider_bike = 0.5;
        final double mass_spoked_wheel = 0.7843;
        final double mass_disk_wheel = 1.018;
        final double radius = 0.3302;
        final double coeff_roll_rest = 0.004;
        final double moi_disk = 0.10;
        final double moi_spoked = 0.0528;
    }

    protected class Constant_Per_Course {
        double wind_speed;
        double wind_direction;
        double air_density;
        double mass_bike_and_rider;
        double mass_rear_wheel;
        double mass_front_wheel;
        double cross_section_rider_and_bike;
        int power;
        boolean rear_disc;
        boolean front_disc;
        double moi_front;
        double moi_rear;
    }

    protected class Var_Per_Instance {
        double grade;
        double bike_direction;
    }

    Constants constants = new Constants();
    Constant_Per_Course const_per_course = new Constant_Per_Course();
    Var_Per_Instance[] situations;

    double step_length = 10;
    double number_steps;

    ArrayList<Double> distances;
    ArrayList<Double> grades;
    ArrayList<Double> angles;

    public static void main(String[] args) {
        Main runner = new Main(false, false);
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter wind speed (m/s):");
        runner.const_per_course.wind_speed = scan.nextDouble();
        System.out.println("Enter wind direction (rad):");
        runner.const_per_course.wind_direction = scan.nextDouble();
        System.out.println("Enter air density:");
        runner.const_per_course.air_density = scan.nextDouble();
        System.out.println("Enter bike and rider mass (kg):");
        runner.const_per_course.mass_bike_and_rider = scan.nextDouble();
        System.out.println("Enter bike and rider cross section (m^2):");
        runner.const_per_course.cross_section_rider_and_bike = scan.nextDouble();
        System.out.println("Enter course file name:");
        String fname = scan.next();
        runner.parseCourse(fname);
        scan.close();
        double time_both_spoked = runner.findTimeForCourse(0, 10, 0);
        runner.const_per_course.rear_disc = true;
        runner.init();
        double time_rear_disk = runner.findTimeForCourse(0, 10, 0);
        runner.const_per_course.front_disc = true;
        runner.init();
        double time_both_disk = runner.findTimeForCourse(0, 10, 0);

        System.out.println("Time 2 spoked: " + time_both_spoked + "s");
        System.out.println("Time rear disk: " + time_rear_disk + "s");
        System.out.println("Time 2 disk: " + time_both_disk + "s");
    }

    public Main(boolean rear_disk, boolean front_disk) {
        this.const_per_course.rear_disc = rear_disk;
        this.const_per_course.front_disc = front_disk;
        this.distances = new ArrayList<>();
        this.angles = new ArrayList<>();
        this.grades = new ArrayList<>();
        this.init();
    }

    public void init() {
        const_per_course.power = 440;
        if (const_per_course.rear_disc) {
            const_per_course.mass_rear_wheel = constants.mass_disk_wheel;
            const_per_course.moi_rear = constants.moi_disk;
        } else {
            const_per_course.mass_rear_wheel = constants.mass_spoked_wheel;
            const_per_course.moi_rear = constants.moi_spoked;
        }

        if (const_per_course.front_disc) {
            const_per_course.mass_front_wheel = constants.mass_disk_wheel;
            const_per_course.moi_front = constants.moi_disk;
        } else {
            const_per_course.mass_front_wheel = constants.mass_spoked_wheel;
            const_per_course.moi_front = constants.moi_spoked;
        }
    }

    private double dragCoefficientDisk(double angle) {
        double angle_deg = angle * 180 / Math.PI;
        return 3.23333333333333 * Math.pow(10, -6) * Math.pow(angle_deg, 3) - 0.0001135 * Math.pow(angle_deg, 2)
                + 0.00011166666666666 * angle_deg + 0.014;
    }

    private double dragCoefficientSpoked(double angle) {
        double angle_deg = angle * 180 / Math.PI;
        return 8.66666669999999 * Math.pow(10, -7) * Math.pow(angle_deg, 3) - 0.00004100000015 * Math.pow(angle_deg, 2)
                + 0.000630000001499999 * angle_deg + 0.0142;
    }

    private double findTimeForCourse(double start_dist, double start_speed, double num_steps) {
        if (pastCourse(start_dist)) {
            return num_steps * step_length;
        }
        System.out.println("Speed: " + start_speed);
        System.out.println("Distance: " + start_dist);
        Var_Per_Instance situation = new Var_Per_Instance();
        situation.bike_direction = getBearing(start_dist);
        situation.grade = getGrade(start_dist);
        double speed = findSpeedForInstance(situation, start_speed);
        double dist = start_dist + speed * step_length;
        System.out.println("Speed after: " + speed);
        System.out.println("Distance after: " + dist);
        return findTimeForCourse(dist, speed, num_steps + 1);
        // return 0;
    }

    private boolean pastCourse(double distance) {
        // System.out.println(distance);
        // System.out.println(distances.get(distances.size() - 1));
        return distance > distances.get(distances.size() - 1);
    }

    private double getGrade(double distance) {
        for (int i = 0; i < distances.size() - 1; i++) {
            if (distance < distances.get(i + 1)) {
                return grades.get(i);
            }
        }
        return 0;
    }

    private double getBearing(double distance) {
        for (int i = 0; i < distances.size() - 1; i++) {
            if (distance < distances.get(i + 1)) {
                return angles.get(i);
            }
        }
        return 0;
    }

    public void parseCourse(String fname) {
        try {
            int i = 0;
            double prev_lat = 0;
            double prev_lon = 0;
            double prev_elev = 0;
            boolean first = true;
            Scanner reader = new Scanner(new FileInputStream(new File(fname)));
            distances.add((Double) 0.0);
            while (reader.hasNextLine()) {
                String ln = reader.nextLine();
                if (ln.contains("<trkpt")) {
                    double lat = Double.parseDouble(ln.split("\"")[1]);
                    double lon = Double.parseDouble(ln.split("\"")[3]);
                    String el = reader.nextLine();
                    double elev = Double.parseDouble(el.substring(el.indexOf(">") + 1, el.lastIndexOf("<")));
                    if (!first) {
                        double elev_change = elev - prev_elev;
                        double d = 2 * constants.earth_radius
                                * Math.asin(Math.sqrt(Math.pow(Math.sin((lat - prev_lat) / 2), 2) +
                                        Math.cos(prev_lat) * Math.cos(lat)
                                                * Math.pow(Math.sin((lon - prev_lon) / 2), 2)));
                        double dt = d / Math.cos(Math.atan(elev_change / d));
                        distances.add(dt + distances.get(i));
                        i++;
                        double bearing = Math.PI / 180
                                * (Math.atan2(Math.sin(lon - prev_lon) * Math.cos(lat),
                                        Math.cos(prev_lat) * Math.sin(lat)
                                                - Math.sin(prev_lat) * Math.cos(lat) * Math.cos(lon - prev_lon))
                                        * 180 / Math.PI + 360)
                                % 360;
                        angles.add(bearing);
                        grades.add(elev_change / d);
                    }
                    prev_elev = elev;
                    prev_lat = lat;
                    prev_lon = lon;
                    first = false;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Problem reading file " + fname);
        }
    }

    private double findSpeedForInstanceRecursive(Var_Per_Instance instance, double start_speed) {
        double wind_v_bike = magnitudeWindvsBike(const_per_course.wind_speed,
                start_speed,
                const_per_course.wind_direction,
                instance.bike_direction);
        double F_g = gravitationalResistance(const_per_course.mass_bike_and_rider, constants.gravity, instance.grade);
        double rotational_velocity = start_speed / constants.radius;
        double F_wa = frictionInAirOnWheels(const_per_course.air_density, rotational_velocity,
                constants.kinematic_viscosity_of_air, constants.radius) / constants.radius;
        double F_drb = dragOnBikeExceptWheels(const_per_course.air_density,
                constants.drag_coeff_rider_bike,
                const_per_course.cross_section_rider_and_bike,
                wind_v_bike,
                const_per_course.wind_speed,
                start_speed,
                const_per_course.wind_direction);
        double angle = const_per_course.wind_direction - instance.bike_direction;
        double F_df = dragOnFrontWheel(
                const_per_course.front_disc ? dragCoefficientDisk(angle) : dragCoefficientSpoked(angle),
                const_per_course.air_density, wind_v_bike);
        double F_dr = dragOnBackWheel(
                const_per_course.rear_disc ? dragCoefficientDisk(angle) : dragCoefficientSpoked(angle),
                const_per_course.air_density, wind_v_bike);
        double F_rr = rollingResistance(constants.coeff_roll_rest, const_per_course.mass_front_wheel
                + const_per_course.mass_rear_wheel + const_per_course.mass_bike_and_rider, constants.gravity,
                instance.grade, start_speed);

        double total_force = F_g + F_wa + F_drb + F_df + F_rr;
        double expected_power = total_force * start_speed;

        if (Math.abs(expected_power - const_per_course.power) < 0.01) {
            return start_speed;
        }

        // didn't get close enough, edit
        if (over == null) {
            over = expected_power > const_per_course.power ? 1 : 0;
        }

        int next_over = expected_power > const_per_course.power ? 1 : 0;

        double speed_change = speed_step * (expected_power > const_per_course.power ? -1 : 1);
        speed_change = speed_change * (over == next_over ? 1 : 0.5);
        over = next_over;
        double next_speed = start_speed + speed_change;

        return findSpeedForInstance(instance, next_speed);
    }

    private double findSpeedForInstance(Var_Per_Instance instance, double start_speed) {
        double w1 = acceleration(instance, start_speed);
        // System.out.println("Acc1: " + w1);
        double w2 = acceleration(instance, start_speed + w1 * step_length / 2);
        double w3 = acceleration(instance, start_speed + w2 * step_length / 2);
        double w4 = acceleration(instance, start_speed + w3 * step_length);
        // System.out.println(start_speed);
        return start_speed + step_length * (w1 + w2 + w3 + w4) / 6;
    }

    private double acceleration(Var_Per_Instance instance, double start_speed) {
        double wind_v_bike = magnitudeWindvsBike(const_per_course.wind_speed,
                start_speed,
                const_per_course.wind_direction,
                instance.bike_direction);
        double F_g = gravitationalResistance(const_per_course.mass_bike_and_rider, constants.gravity, instance.grade);
        double rotational_velocity = start_speed / constants.radius;
        double F_wa = frictionInAirOnWheels(const_per_course.air_density, rotational_velocity,
                constants.kinematic_viscosity_of_air, constants.radius) / constants.radius;
        double F_drb = dragOnBikeExceptWheels(const_per_course.air_density,
                constants.drag_coeff_rider_bike,
                const_per_course.cross_section_rider_and_bike,
                wind_v_bike,
                const_per_course.wind_speed,
                start_speed,
                const_per_course.wind_direction);
        double angle = const_per_course.wind_direction - instance.bike_direction;
        double F_df = dragOnFrontWheel(
                const_per_course.front_disc ? dragCoefficientDisk(angle) : dragCoefficientSpoked(angle),
                const_per_course.air_density, wind_v_bike);
        double F_dr = dragOnBackWheel(
                const_per_course.rear_disc ? dragCoefficientDisk(angle) : dragCoefficientSpoked(angle),
                const_per_course.air_density, wind_v_bike);
        double F_rr = rollingResistance(constants.coeff_roll_rest, const_per_course.mass_front_wheel
                + const_per_course.mass_rear_wheel + const_per_course.mass_bike_and_rider, constants.gravity,
                instance.grade, start_speed);
        // System.out.println("Forces: " + F_g + " " + rotational_velocity + " " + F_wa
        // + " " + F_drb + " " + angle + " " + F_df + " " + F_dr + " " + F_rr);
        // System.out.println("pwr/speed: " + const_per_course.power/start_speed);
        // System.out.println("moi expression: " + (const_per_course.moi_front +
        // const_per_course.moi_rear)/(constants.radius*constants.radius));
        // System.out.println("sum forces: " + (const_per_course.power/start_speed - F_g
        // - F_wa - F_drb - F_df - F_dr - F_rr));
        // System.out.println("denominator: " + (const_per_course.mass_front_wheel +
        // const_per_course.mass_rear_wheel + const_per_course.mass_bike_and_rider +
        // (const_per_course.moi_front +
        // const_per_course.moi_rear)/(constants.radius*constants.radius)));
        return (const_per_course.power / start_speed - F_g - F_wa - F_drb - F_df - F_dr - F_rr)
                / (const_per_course.mass_front_wheel + const_per_course.mass_rear_wheel
                        + const_per_course.mass_bike_and_rider
                        + (const_per_course.moi_front + const_per_course.moi_rear)
                                / (constants.radius * constants.radius));
    }

    private double dragOnBikeExceptWheels(double rho, double c_rb, double A, double v_wb, double v_wg, double v_bg,
            double gamma) {
        return 0.5 * rho * c_rb * A * v_wb
                * Math.cos(
                        Math.atan(
                                (v_wg * Math.sin(gamma)) / (v_bg - v_wg * Math.cos(gamma))));
    }

    private double dragOnFrontWheel(double c_f, double rho, double v_wb) {
        return 0.5 * rho * c_f * v_wb * v_wb;
    }

    private double dragOnBackWheel(double c_r, double rho, double v_wb) {
        return 0.5 * 0.75 * rho * c_r * v_wb;
    }

    private double gravitationalResistance(double m, double g, double psi) {
        return m * g * psi;
    }

    private double rollingResistance(double c_rr, double m, double g, double psi, double v_bg) {
        if (v_bg == 0) {
            return 0;
        } else {
            return c_rr * m * g * Math.sqrt(1 - psi * psi);
        }
    }

    private double frictionInAirOnWheels(double rho, double omega, double mu, double r) {
        return 0.616 * Math.PI * rho * Math.pow(omega, 1.5) * Math.pow(Math.abs(mu), 0.5) * Math.pow(r, 4);
    }

    private double magnitudeWindvsBike(double v_wg, double v_bg, double wind_direction, double rider_direction) {
        return v_wg * Math.cos((wind_direction - rider_direction) * Math.PI / 180) + v_bg;
    }

}
