function toggleMenu() {
    var menu = document.getElementById("sideMenu");
    if (menu.classList.contains('show-menu')) {
        menu.classList.remove('show-menu');
        menu.classList.add('hide-menu');
    } else {
        menu.classList.remove('hide-menu');
        menu.classList.add('show-menu');
    }
}