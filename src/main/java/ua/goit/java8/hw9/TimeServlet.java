package ua.goit.java8.hw9;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

@WebServlet(urlPatterns = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "text/html; charset=utf-8");

        Map<String, Object> params = new LinkedHashMap<>();

        Integer zone = zoneParser(req);
        String nowStr = getStrDate(zone);

        params.put("data", nowStr);
        params.put("timeZone", zone);

        Context context = new Context(
                req.getLocale(),
                Map.of("queryParams", params)
        );

        if (zone != null) {
            resp.addCookie(new Cookie("lastTimezone", zone.toString()));
        }
        engine.process("timeTemplate", context, resp.getWriter());
        resp.getWriter().close();
    }

    private String getStrDate(Integer zone) {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String nowStr = format.format(date);

        if (zone != null) {
            if (zone >= -12 & zone <= 12) {
                long msDate = date.getTime() + zone * 3600000;
                date = new Date(msDate);
                nowStr = format.format(date);
            }

            if (zone > 0) {
                nowStr = nowStr + "+" + zone;
            } else {
                nowStr = nowStr + zone;
            }
        }
        return nowStr;
    }

    private Integer zoneParser(HttpServletRequest req) {
        Integer result = null;
        String timezone = req.getParameter("timezone");
        if (timezone != null) {
            String substring = timezone.substring(3);
            if (substring.startsWith(" ")) {
                substring = substring.substring(1);
            }
            result = Integer.parseInt(substring);
        } else {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie cookie: cookies) {
                    if (cookie.getName().equals("lastTimezone")) {
                        result = Integer.parseInt(cookie.getValue());
                    }
                }
            }
        }
        return result;
    }

}
