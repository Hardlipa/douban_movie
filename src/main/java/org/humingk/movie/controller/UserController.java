package org.humingk.movie.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.humingk.movie.common.JsonUtil;
import org.humingk.movie.common.ResultMessage;
import org.humingk.movie.entity.User;
import org.humingk.movie.entity.UserMovie;
import org.humingk.movie.service.ShiroService;
import org.humingk.movie.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author humin
 */
@Controller
@RequestMapping(value = "/")
public class UserController {

    @Autowired
    private ShiroService shiroService;
    @Autowired
    private UserService userService;

    private static final String USER = "user";
    private static final String ADMIN = "admin";

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    /**
     * 登录表单处理
     *
     * @param email
     * @param password
     * @param modelAndView
     * @return
     */
    @RequestMapping(value = "loginForm", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView login(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              @RequestParam(value = "rememberMe", defaultValue = "false") boolean rememberMe,
                              ModelAndView modelAndView) {
        if ("".equals(email)) {
            modelAndView.addObject("login", ResultMessage.createMessage(200, "null email", null));
            modelAndView.setViewName("login");
        }
        User userInfo = userService.getUserInfoByUserEmail(email);
        // 封装表单,存入shiro的token
        // 为了加入 rememberMe 选项， 需要让 user 实体类 实现 序列化接口
        UsernamePasswordToken token = new UsernamePasswordToken(email, password, rememberMe);
        // Shiro 实现登录
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        try {
            // 登录前先清除当前session的currentUser
            session.removeAttribute("currentUser");
            subject.login(token);
            // 登录后，添加session currentUser
            // 便于全局获取Userinfo
            session.setAttribute("currentUser", JsonUtil.toJson(userInfo));

            logger.info("登录成功,添加session");
            logger.info("session sessionID: "+session.getId());
            logger.info("session  account: "+((User)subject.getPrincipal()).getEmail());
            logger.info("session currentUser: "+session.getAttribute("currentUser").toString());

            // 管理员用户
            if (subject.hasRole(ADMIN)) {
                modelAndView.setViewName("redirect:/people/" + userInfo.getLabel());
            }
            // 普通注册用户
            else if (subject.hasRole(USER)) {
                // 普通用户有域名
                if (userInfo.getLabel() != null) {
                    modelAndView.setViewName("redirect:/people/" + userInfo.getLabel());
                }
                // 普通用户没有域名 只有ID
                else {
                    modelAndView.setViewName("redirect:/people/" + userInfo.getUserId());
                }
            }
        } catch (AuthenticationException e) {
            // 登陆失败
            modelAndView.setViewName("redirect:/login");
            e.printStackTrace();
        }
        return modelAndView;
    }

    /**
     * 用户注册
     *
     * @param email
     * @param label
     * @param password
     * @param modelAndView
     * @return
     */
    @RequestMapping(value = "/registerForm", method = RequestMethod.POST)
    public ModelAndView register(@RequestParam("email") String email,
                                 @RequestParam("name") String name,
                                 @RequestParam("label") String label,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("password") String password,
                                 ModelAndView modelAndView) {
        try {
            User user = new User(email, label, name, password, phone);
            shiroService.insertNormalUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        modelAndView.setViewName("redirect:/people/" + label);
        return modelAndView;
    }

    /**
     * 登录页面 url处理
     *
     * @return
     */
    @RequestMapping(value = "login")
    public String loginPage() {
        return "login";
    }


    /**
     * 更新用户 wish seen
     *
     * @return
     */
    @RequestMapping(value = "updateWishAndSeen")
    public List<UserMovie> updateWishAndSeen() {

        return null;
    }


    /**
     * 有权限访问个人主页
     *
     * @return
     */
    @RequestMapping(value = "people")
    public String people() {
        return "people";
    }

    /**
     * 个人权限认证失败跳转页
     *
     * @return
     */
    @RequestMapping(value = "unauthor")
    public String unauthor() {
        return "unauthor";
    }

    /**
     * 没有权限跳转页
     *
     * @return
     */
    @RequestMapping(value = "noPermission/**")
    public String noPermission() {
        return "noPermission";
    }

}
