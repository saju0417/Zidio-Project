package com.zidio.util;

import com.zidio.entity.Interview;
import com.zidio.entity.User;

import java.time.format.DateTimeFormatter;

public class AdminEmailTemplateUtil {

    public static String interviewScheduledAdminTemplate(User recruiter, User candidate, Interview interview) {
        String formattedDate = interview.getScheduledAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        return """
            <html>
            <body style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 20px;'>
                <table align='center' width='600' style='background:#fff;border-radius:10px;box-shadow:0 3px 8px rgba(0,0,0,0.1);padding:30px;'>
                    <tr>
                        <td align='center'>
                            <h2 style='color:#0f172a;'>Zidio Connect Admin Alert</h2>
                            <h3 style='color:#14b8a6;'>New Interview Scheduled</h3>
                            <hr style='border:none;height:1px;background:#e5e7eb;margin:20px 0;'/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <p>Dear <b>Admin</b>,</p>
                            <p>A new interview has been scheduled through the Zidio Connect platform.</p>
                            <table width='100%%' style='margin-top:10px;margin-bottom:10px;border-collapse:collapse;'>
                                <tr><td style='padding:5px 0;'>🧑 <b>Recruiter:</b></td><td>%s (%s)</td></tr>
                                <tr><td style='padding:5px 0;'>🎓 <b>Candidate:</b></td><td>%s (%s)</td></tr>
                                <tr><td style='padding:5px 0;'>💼 <b>Job ID:</b></td><td>%d</td></tr>
                                <tr><td style='padding:5px 0;'>📅 <b>Date & Time:</b></td><td>%s</td></tr>
                                <tr><td style='padding:5px 0;'>💻 <b>Mode:</b></td><td>%s</td></tr>
                                <tr><td style='padding:5px 0;'>🔗 <b>Location / Link:</b></td><td><a href='%s'>%s</a></td></tr>
                            </table>
                            <p style='margin-top:20px;'>Please ensure the interview details are logged and verified in the admin dashboard.</p>
                            <p style='margin-top:20px;'>Best Regards,<br/><b>Zidio Connect System</b></p>
                            <hr style='border:none;height:1px;background:#e5e7eb;margin:20px 0;'/>
                            <small style='color:#94a3b8;'>This is an automated system alert. Do not reply to this email.</small>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
        """.formatted(
                recruiter.getFullName(), recruiter.getEmail(),
                candidate.getFullName(), candidate.getEmail(),
                interview.getJobId(),
                formattedDate,
                interview.getMode(),
                interview.getLocation(), interview.getLocation()
        );
    }
}
