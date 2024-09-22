
class Main {

    protected class Constants {
        final double gravity = 9.81;
        final double kinematic_velocity_of_air = 0;
        final double cross_section_spoked_wheel = 0;
        final double cross_section_disc_wheel = 0;
        final double drag_coeff_spoked_wheel = 0;
        final double drag_coeff_disc_wheel = 0;
        final double drag_coeff_rider_bike = 0;
        final double mass_spoked_wheel = 0;
        final double mass_disk_wheel = 0;
        final double radius = 0;
        final double coeff_roll_rest = 0;
        final double moi_disk = 0;
        final double moi_spoked = 0;
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
        double cross_section_rear_wheel;
        double cross_section_front_wheel;
        double drag_coeff_rear_wheel;
        double drag_coeff_front_wheel;
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

    double step_length;
    double number_steps;

    public static void main(String[] args) {

    }

    public void init() {
        if (const_per_course.rear_disc) {
            const_per_course.cross_section_rear_wheel = constants.cross_section_disc_wheel;
            const_per_course.drag_coeff_rear_wheel = constants.drag_coeff_disc_wheel;
            const_per_course.mass_rear_wheel = constants.mass_disk_wheel;
            const_per_course.moi_rear = constants.moi_disk;
        } else {
            const_per_course.cross_section_rear_wheel = constants.cross_section_spoked_wheel;
            const_per_course.drag_coeff_rear_wheel = constants.drag_coeff_spoked_wheel;
            const_per_course.mass_rear_wheel = constants.mass_spoked_wheel;
            const_per_course.moi_rear = constants.moi_spoked;
        }

        if (const_per_course.front_disc) {
            const_per_course.cross_section_front_wheel = constants.cross_section_disc_wheel;
            const_per_course.drag_coeff_front_wheel = constants.drag_coeff_disc_wheel;
            const_per_course.mass_front_wheel = constants.mass_disk_wheel;
            const_per_course.moi_front = constants.moi_disk;
        } else {
            const_per_course.cross_section_front_wheel = constants.cross_section_spoked_wheel;
            const_per_course.drag_coeff_front_wheel = constants.drag_coeff_spoked_wheel;
            const_per_course.mass_front_wheel = constants.mass_spoked_wheel;
            const_per_course.moi_front = constants.moi_spoked;
        }
    }

    private double findSpeedForInstance(Var_Per_Instance instance, double start_speed) {
        double w1 = acceleration(instance, start_speed);
        double w2 = acceleration(instance, start_speed+w1*step_length/2);
        double w3 = acceleration(instance, start_speed+w2*step_length/2);
        double w4 = acceleration(instance, start_speed+w3*step_length);
        return start_speed + step_length*(w1+w2+w3+w4)/6;
    }

    private double acceleration(Var_Per_Instance instance, double start_speed){
        double wind_v_bike = magnitudeWindvsBike(const_per_course.wind_speed, 
                                                start_speed, 
                                                const_per_course.wind_direction, 
                                                instance.bike_direction);
        double F_g = gravitationalResistance(const_per_course.mass_bike_and_rider, constants.gravity, instance.grade);
        double rotational_velocity = start_speed / constants.radius;
        double F_wa = frictionInAirOnWheels(const_per_course.air_density, rotational_velocity, F_g, start_speed)/constants.radius;
        double F_drb = dragOnBikeExceptWheels(const_per_course.air_density, 
                                            constants.drag_coeff_rider_bike, 
                                            const_per_course.cross_section_rider_and_bike, 
                                            wind_v_bike, 
                                            const_per_course.wind_speed, 
                                            start_speed, 
                                            const_per_course.wind_direction);
        double F_df = dragOnFrontWheel(constants.drag_coeff_spoked_wheel, constants.cross_section_spoked_wheel, const_per_course.air_density, wind_v_bike);
        double F_dr = dragOnBackWheel(const_per_course.drag_coeff_rear_wheel, const_per_course.cross_section_rear_wheel, const_per_course.air_density, wind_v_bike);
        double F_rr = rollingResistance(constants.coeff_roll_rest, const_per_course.mass_front_wheel + const_per_course.mass_rear_wheel + const_per_course.mass_bike_and_rider, constants.gravity, instance.grade, start_speed);
        return (const_per_course.power/start_speed - F_g - F_wa - F_drb - F_df - F_dr - F_rr)/(const_per_course.mass_front_wheel + const_per_course.mass_rear_wheel + const_per_course.mass_bike_and_rider + (const_per_course.moi_front + const_per_course.moi_rear)/(constants.radius*constants.radius));
    }

    private double dragOnBikeExceptWheels(double rho, double c_rb, double A, double v_wb, double v_wg, double v_bg,
            double gamma) {
        return 0.5 * rho * c_rb * A * v_wb
                * Math.cos(
                        Math.atan(
                                (v_wg * Math.sin(gamma)) / (v_bg - v_wg * Math.cos(gamma))));
    }

    private double dragOnFrontWheel(double c_f, double A_f, double rho, double v_wb) {
        return 0.5 * rho * c_f * A_f * v_wb * v_wb;
    }

    private double dragOnBackWheel(double c_r, double A_r, double rho, double v_wb) {
        return 0.5 * 0.75 * rho * c_r * A_r * v_wb;
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
        return 0.616 * Math.PI * Math.pow(rho, 1.5) * Math.pow(mu, 0.5) * Math.pow(r, 4);
    }

    private double magnitudeWindvsBike(double v_wg, double v_bg, double wind_direction, double rider_direction){
        return v_wg*Math.cos(wind_direction-rider_direction)+v_bg;
    }

}
