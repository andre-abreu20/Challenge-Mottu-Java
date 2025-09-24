package fiap.com.br.autottu.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Entrar");
        return "auth/login"; // templates/auth/login.html
    }

    @GetMapping("/logout-success")
    public String logoutSuccess(Model model) {
        model.addAttribute("pageTitle", "Sessão encerrada");
        return "auth/logout-success"; // templates/auth/logout-success.html (opcional)
    }
}