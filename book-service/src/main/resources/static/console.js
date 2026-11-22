const journal = [];
function log(msg) {
  journal.unshift(new Date().toLocaleTimeString() + "  " + msg);
  document.getElementById("journal").textContent = journal.slice(0, 40).join("\n");
}

function kv(entries) {
  return entries.map(([k, v]) => "<dt>" + k + "</dt><dd>" + v + "</dd>").join("");
}

async function charger() {
  const r = await fetch("/api/console/state");
  const s = await r.json();

  document.getElementById("instance").textContent =
    "instance " + s.instance.hostname + " · :" + s.instance.internalPort;

  const cb = s.circuitBreaker;
  const cartes = [
    { label: "Circuit breaker", value: cb.state, unite: "pricing" },
    { label: "Taux d'echec", value: cb.failureRate === -1 ? "-" : cb.failureRate + "%", unite: cb.failedCalls + "/" + cb.bufferedCalls },
    { label: "Retries effectues", value: s.retry.failedCallsWithRetryAttempt, unite: "tentatives" },
    { label: "Pricing joignable", value: s.pricing.reachable ? "OUI" : "NON", unite: s.pricing.baseUrl }
  ];
  document.getElementById("kpis").innerHTML = cartes.map(c =>
    "<div class='kpi'><div class='label'>" + c.label + "</div><div class='value'>" +
    c.value + " <small>" + c.unite + "</small></div></div>").join("");

  document.getElementById("cb").innerHTML = kv([
    ["etat", "<span class='etat-" + cb.state + "'>" + cb.state + "</span>"],
    ["appels enregistres", cb.bufferedCalls],
    ["echecs", cb.failedCalls],
    ["succes", cb.successfulCalls],
    ["taux d'echec", cb.failureRate === -1 ? "n/a" : cb.failureRate + " %"]
  ]);

  document.getElementById("retry").innerHTML = kv([
    ["echecs avec retry", s.retry.failedCallsWithRetryAttempt],
    ["echecs sans retry", s.retry.failedCallsWithoutRetryAttempt],
    ["succes avec retry", s.retry.successfulCallsWithRetryAttempt]
  ]);

  document.getElementById("pricing-url").textContent = s.pricing.baseUrl;
  if (s.pricing.reachable && s.pricing.stats) {
    document.getElementById("pricing").innerHTML = kv([
      ["service", s.pricing.stats.service],
      ["appels recus", s.pricing.stats.calls],
      ["echecs", s.pricing.stats.failures],
      ["mode chaos", s.pricing.stats.chaos ? "ACTIF" : "inactif"],
      ["seuil de panne", s.pricing.stats.failurePercent + " %"]
    ]);
  } else {
    document.getElementById("pricing").innerHTML = kv([
      ["etat", "<span class='etat-OPEN'>INJOIGNABLE</span>"],
      ["erreur", s.pricing.erreur || "n/a"]
    ]);
  }

  document.querySelector("#books tbody").innerHTML = s.books.map(b =>
    "<tr><td>" + b.id + "</td><td>" + b.title + "</td><td>" + b.author + "</td><td>" +
    b.stock + "</td><td>" + b.version + "</td>" +
    "<td><button class='ok' data-borrow='" + b.id + "'>emprunter</button></td></tr>").join("")
    || "<tr><td colspan='6'>Aucun livre</td></tr>";

  document.querySelectorAll("[data-borrow]").forEach(btn => {
    btn.addEventListener("click", async () => {
      const id = btn.getAttribute("data-borrow");
      const rep = await fetch("/api/books/" + id + "/borrow", { method: "POST" });
      const corps = await rep.json();
      log("emprunt #" + id + " -> HTTP " + rep.status + " " + JSON.stringify(corps));
      charger();
    });
  });
}

document.getElementById("chaos-on").addEventListener("click", async () => {
  const r = await fetch("/api/console/chaos?enabled=true&percent=100", { method: "POST" });
  log("chaos ACTIVE -> " + JSON.stringify(await r.json()));
  charger();
});
document.getElementById("chaos-off").addEventListener("click", async () => {
  const r = await fetch("/api/console/chaos?enabled=false&percent=0", { method: "POST" });
  log("chaos DESACTIVE -> " + JSON.stringify(await r.json()));
  charger();
});
document.getElementById("creer").addEventListener("click", async () => {
  const corps = {
    title: document.getElementById("titre").value,
    author: document.getElementById("auteur").value,
    stock: parseInt(document.getElementById("stock").value || "0", 10)
  };
  const r = await fetch("/api/books", {
    method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(corps)
  });
  log("creation -> HTTP " + r.status + " " + JSON.stringify(await r.json()));
  charger();
});
document.getElementById("refresh").addEventListener("click", charger);
charger();
