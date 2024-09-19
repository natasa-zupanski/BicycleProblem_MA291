
class Main {

    protected static class Constants {
        final double gravity = 9.81;
        final double kinematic_velocity_of_air = 0;
        final double cross_section_spoked_wheel = 0;
        final double cross_section_disc_wheel = 0;
        final double radius_spoked_wheel = 0;
        final double radius_disc_wheel = 0;
    }

    protected class Constant_Per_Course {
        double wind_speed;
        double wind_direction;
        double air_direction;
        double mass_bike_and_rider;
        double cross_section_rider_and_bike;
        int power;
    }

    protected class Var_Per_Instance {
        double grade;
        double bike_direction;
    }

    protected class Inputs {
        double step_length;
        double number_steps;
        Var_Per_Instance[] situations;
        Constant_Per_Course course_specs;
    }

    public static void main(String[] args) {

    }

    private double dragOnBikeExceptWheels(double rho, double c_rb, double A, double v_wb, double v_wg, double v_bg,
            double gamma) {
        return 0.5 * rho * c_rb * A * v_wb
                * Math.cos(
                        Math.atan(
                                (v_wg * Math.sin(gamma)) / (v_bg - v_wg * Math.cos(gamma))));
    }

    private double dragOnFrontWheel(double c_f, double A_f, double rho, double v_wb) {
        return 0.5 * c_f * A_f * v_wb * v_wb;
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

}
