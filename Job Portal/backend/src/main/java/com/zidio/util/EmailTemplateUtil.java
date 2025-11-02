package com.zidio.util;

import com.zidio.entity.Interview;
import com.zidio.entity.User;
import java.time.format.DateTimeFormatter;

public class EmailTemplateUtil {

    public static String interviewScheduledTemplate(User candidate, Interview interview) {
        String formattedDate = interview.getScheduledAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        return """
            <html>
            <body style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 20px;'>
                <table align='center' width='600' style='background:#fff;border-radius:10px;box-shadow:0 3px 8px rgba(0,0,0,0.1);padding:30px;'>
                    <tr>
                        <td align='center'>
                            <h2 style='color:#0f172a;'>Zidio Connect</h2>
                            <h3 style='color:#14b8a6;'>Interview Scheduled</h3>
                            <hr style='border:none;height:1px;background:#e5e7eb;margin:20px 0;'/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <p>Hi <b>%s</b>,</p>
                            <p>Your interview has been successfully scheduled through <b>Zidio Connect</b>.</p>
                            <table width='100%%' style='margin-top:10px;margin-bottom:10px;border-collapse:collapse;'>
                                <tr><td style='padding:5px 0;'>📅 <b>Date & Time:</b></td><td>%s</td></tr>
                                <tr><td style='padding:5px 0;'>💻 <b>Mode:</b></td><td>%s</td></tr>
                                <tr><td style='padding:5px 0;'>🔗 <b>Link / Location:</b></td><td><a href='%s'>%s</a></td></tr>
                            </table>
                            <p>Please be on time and ensure your internet connection is stable.</p>
                            <p>Best of luck,<br><b>Zidio Connect Team</b></p>
                            <hr style='border:none;height:1px;background:#e5e7eb;margin:20px 0;'/>
                            <small style='color:#94a3b8;'>This is an automated message. Do not reply to this email.</small>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
        """.formatted(candidate.getFullName(), formattedDate, interview.getMode(), interview.getLocation(), interview.getLocation());
    }
}
