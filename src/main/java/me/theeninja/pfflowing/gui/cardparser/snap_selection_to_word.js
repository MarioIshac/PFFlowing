(function snapSelectionToWord() {
    var sel;

    // Check for existence of window.getSelection() and that it has a
    // modify() method. IE 9 has both selection APIs but no modify() method.
    if (window.getSelection && (sel = window.getSelection()).modify) {
        sel = window.getSelection();
        if (!sel.isCollapsed) {

            // Detect if selection is backwards
            var range = document.createRange();
            range.setStart(sel.anchorNode, sel.anchorOffset);
            range.setEnd(sel.focusNode, sel.focusOffset);
            var backwards = range.collapsed;
            range.detach();

            // modify() works on the focus of the selection
            var endNode = sel.focusNode, endOffset = sel.focusOffset;
            sel.collapse(sel.anchorNode, sel.anchorOffset);
            if (backwards) {
                sel.modify("move", "backward", "character");
                sel.modify("move", "forward", "word");
                sel.extend(endNode, endOffset);
                sel.modify("extend", "forward", "character");
                sel.modify("extend", "backward", "word");

            } else {
                sel.modify("move", "forward", "character");
                sel.modify("move", "backward", "word");
                sel.extend(endNode, endOffset);
                sel.modify("extend", "backward", "character");
                sel.modify("extend", "forward", "word");
            }
        }
    } else if ( (sel = document.selection) && sel.type !== "Control") {
        var textRange = sel.createRange();
        if (textRange.text) {
            textRange.expand("word");
            // Move the end back to not include the word's trailing space(s),
            // if necessary
            while (/\s$/.test(textRange.text)) {
                textRange.moveEnd("character", -1);
            }
            textRange.select();
        }
    }
})();