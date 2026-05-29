function interactEvent(event) {
    if (event.type !== 2) return;
    if (event.target.getName() !== "Labyrinthe") return;

    var player = event.player;

    if (!player.hasTempData("tpDone")) {
        player.setTempData("tpDone", true);
        player.sendMessage("§aTéléportation vers le labyrinthe...");
        player.setPosition(-339, 70, 497);
    } else {
        player.sendMessage("§cVous êtes déjà allé au labyrinthe.");
    }
}
