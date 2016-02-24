function treeColapse(icon) {
    var parentNode = icon.parentNode;
    var allChildren = parentNode.childNodes;
    var todoOther = "";
    var attributes = "";
    //Do minimizing for the rest of the nodes
    for (var i = 0; i < allChildren.length; i++) {
        if (allChildren[i].nodeName == "UL") {

            if (allChildren[i].style.display == "none") {
                attributes = {
                    opacity: { to: 1 }
                };
                var anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.animate();
                allChildren[i].style.display = "";
                if (YAHOO.util.Dom.hasClass(icon, "plus") || YAHOO.util.Dom.hasClass(icon, "minus")) {
                    YAHOO.util.Dom.removeClass(icon, "plus");
                    YAHOO.util.Dom.addClass(icon, "minus");
                }
                todoOther = "show";
                parentNode.style.height = "auto";
            }
            else {
                attributes = {
                    opacity: { to: 0 }
                };
                anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.duration = 0.3;
                anim.onComplete.subscribe(hideTreeItem, allChildren[i]);

                anim.animate();
                if (YAHOO.util.Dom.hasClass(icon, "plus") || YAHOO.util.Dom.hasClass(icon, "minus")) {
                    YAHOO.util.Dom.removeClass(icon, "minus");
                    YAHOO.util.Dom.addClass(icon, "plus");
                }
                todoOther = "hide";
                //parentNode.style.height = "50px";
            }
        }
    }
}
function hideTreeItem(state,opts,item){
   item.style.display = "none"; 
}
function selectMe(obj){
    if(YAHOO.util.Dom.hasClass(obj, 'selected')){
        YAHOO.util.Dom.removeClass(obj, 'selected');
    } else {
        YAHOO.util.Dom.addClass(obj, 'selected');
    }
}
var paths = new Array();
function pickNames(fullPath){
    var nameLinks = YAHOO.util.Dom.getElementsByClassName('selected', 'a');
    var listView = document.getElementById('listView');
    var displayNodeValue;
    for(var i=0;i<nameLinks.length;i++){
        var path = getTreePathFromNode(nameLinks[i],nameLinks[i].innerHTML);
        var newNode = document.createElement("DIV");
        newNode.className = "listViewItem";
        var isSelected = false;
        for(var j in paths){
            if(path == paths[j].path){
                isSelected = true;
            }
        }               
        if(!isSelected){
            paths.push({path:path,name:nameLinks[i].innerHTML});
            newNode.title = path;
            newNode.id = nameLinks[i].innerHTML;
            if(fullPath){
                displayNodeValue = path;
            } else {
                displayNodeValue = nameLinks[i].innerHTML;
            }
            var delLink = '<a onclick="remvoeMe(this)" class="listViewItemDel"><img src="images/close.png" border="0" /></a><div style="clear:both"></div>';
            newNode.innerHTML = '<div class="listViewItemContent">'+ displayNodeValue +'</div>' + delLink;
            listView.appendChild(newNode);
        }

    }
    return;
}
function getTreePathFromNode(node,path){
    var hasParent = false;
    if(node.nodeName == "A"){
        var parentOnTree = node.parentNode.parentNode.parentNode;
        if(parentOnTree.nodeName == "DIV"){
            return path;
        }
        var allChildren = parentOnTree.childNodes;
        var nodeNumber = 0;
        for(var i=0;i<allChildren.length;i++){
            if(allChildren[i].nodeName != "#text" &&  allChildren[i].nodeName != "#comment" && allChildren[i].nodeName == "A"){
                nodeNumber++;
                if(nodeNumber == 2){
                    path = allChildren[i].innerHTML + "/" + path;
                    parentOnTree = allChildren[i];
                    hasParent = true;
                }
            }
        }

    }


    if(hasParent){
       path = getTreePathFromNode(parentOnTree,path);
    }
    return path;
}
function remvoeMe(me){
    me.parentNode.parentNode.removeChild(me.parentNode);
    var removeElement;
    for(var i in paths){
        if(me.parentNode.title == paths[i].path){
            removeElement = i;
            break;
        }
    }

    delete paths[removeElement];

    return;
}                                                