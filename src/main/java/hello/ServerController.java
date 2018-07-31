package hello;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ServerController {

    private final String defaultArea = "Any";

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/map")
    public String map(@RequestParam(value = "area", required = false, defaultValue = defaultArea) String area, Model model) {

        model.addAttribute("area", area);
        return "map";
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "")
    public String index() {

        return "index";
    }
}