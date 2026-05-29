// Configuration des points de téléportation
var TP_POINTS = {
    "labyrinthe": { x: -339, y: 70, z: 497, dim: 0, msg: "§aTéléportation vers le labyrinthe..." },
    "spawn":      { x: 0, y: 64, z: 0, dim: 0, msg: "§aTéléportation au spawn..." },
    "nether":     { x: 0, y: 80, z: 0, dim: -1, msg: "§cTéléportation au Nether..." },
    "end":        { x: 0, y: 60, z: 0, dim: 1, msg: "§dTéléportation à l'End..." },
    "city":       { x: -200, y: 70, z: 400, dim: 0, msg: "§aTéléportation à la ville..." }
};

// Cooldown global (en ticks, 20 ticks = 1 seconde)
var COOLDOWN_TICKS = 60;

// Fonction principale de téléportation
function teleportPlayer(player, pointName) {
    var point = TP_POINTS[pointName];
    if (!point) {
        player.sendMessage("§cPoint de téléportation '" + pointName + "' inconnu.");
        return false;
    }

    var cooldownKey = "tpCooldown_" + pointName;
    var currentTick = API.getServerTime();

    if (player.hasTempData("tpWaiting")) {
        player.sendMessage("§cVous êtes déjà en cours de téléportation.");
        return false;
    }

    if (player.hasTempData(cooldownKey)) {
        var lastUsed = player.getTempData(cooldownKey);
        if (currentTick - lastUsed < COOLDOWN_TICKS) {
            var remaining = Math.ceil((COOLDOWN_TICKS - (currentTick - lastUsed)) / 20);
            player.sendMessage("§cVeuillez patienter " + remaining + " seconde(s).");
            return false;
        }
    }

    player.setTempData("tpWaiting", true);
    player.sendMessage(point.msg);

    // Si changement de dimension
    if (point.dim !== undefined && point.dim !== player.getDimention()) {
        player.setPosition(point.x, point.y, point.z, point.dim);
    } else {
        player.setPosition(point.x, point.y, point.z);
    }

    player.setTempData(cooldownKey, currentTick);
    player.setTempData("tpWaiting", false);

    return true;
}

// Event: Quand un joueur parle dans le chat
function chatEvent(event) {
    var msg = event.message;
    var player = event.player;

    // Commande: !tp <nom>
    if (msg.startsWith("!tp ")) {
        var pointName = msg.substring(4).trim().toLowerCase();
        if (teleportPlayer(player, pointName)) {
            event.setCanceled(true);
        }
        return;
    }

    // Commande: !tplist
    if (msg === "!tplist") {
        var list = "§6Points disponibles: §e";
        var first = true;
        for (var name in TP_POINTS) {
            if (!first) list += "§7, §e";
            list += name;
            first = false;
        }
        player.sendMessage(list);
        event.setCanceled(true);
    }
}

// Event: Quand un joueur interagit avec un NPC
function interactEvent(event) {
    if (event.type === 2) { // NPC
        var npc = event.target;
        var player = event.player;

        if (npc.getName && npc.getName() === "Teleporteur") {
            teleportPlayer(player, "spawn");
        }
    }
}
