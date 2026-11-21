// Live mortgage calculator on the listing page (external file so the CSP can forbid inline scripts).
(function () {
    'use strict';
    var root = document.getElementById('mortcalc');
    if (!root) {
        return;
    }
    var ids = ['mc-price', 'mc-deposit', 'mc-rate', 'mc-term'];
    var fmt = new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 });

    function val(id) {
        var n = parseFloat(document.getElementById(id).value);
        return isNaN(n) ? 0 : n;
    }

    function update() {
        var price = val('mc-price');
        var loan = price * (1 - val('mc-deposit') / 100);
        var r = val('mc-rate') / 100 / 12;
        var n = val('mc-term') * 12;
        var monthly = n <= 0 ? 0 : (r === 0 ? loan / n : loan * r / (1 - Math.pow(1 + r, -n)));
        document.getElementById('mc-output').textContent = fmt.format(monthly);
        document.getElementById('mc-loan').textContent = 'on a mortgage of ' + fmt.format(loan);
    }

    ids.forEach(function (id) {
        document.getElementById(id).addEventListener('input', update);
    });
    update();
})();
