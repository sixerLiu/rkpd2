/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.rkpd2.items.spells;

import com.watabou.noosa.ui.Component;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.npcs.Shopkeeper;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.potions.AlchemicalCatalyst;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.AlchemyScene;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.RedButton;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndBag;
import com.zrp200.rkpd2.windows.WndEnergizeItem;
import com.zrp200.rkpd2.windows.WndImp;
import com.zrp200.rkpd2.windows.WndInfoItem;
import com.zrp200.rkpd2.windows.WndTradeItem;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class Alchemize extends Spell {
	
	{
		image = ItemSpriteSheet.ALCHEMIZE;
	}

	private static WndBag parentWnd;
	
	@Override
	protected void onCast(Hero hero) {
		parentWnd = GameScene.selectItem( itemSelector );
	}
	
	@Override
	public int value() {
		//prices of ingredients, divided by output quantity
		return Math.round(quantity * (40 / 8f));
	}

	//TODO also allow alchemical catalyst? Or save that for an elixir/brew?
	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {

		{
			inputs =  new Class[]{ArcaneCatalyst.class};
			inQuantity = new int[]{1};
			
			cost = 2;
			
			output = Alchemize.class;
			outQuantity = 8;
		}

	}

	private static WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(Alchemize.class, "prompt");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return !(item instanceof Alchemize)
					&& (Shopkeeper.canSell(item) || item.energyVal() > 0);
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				if (parentWnd != null) {
					parentWnd = GameScene.selectItem(itemSelector);
				}
				GameScene.show( new WndAlchemizeItem( item, parentWnd ) );
			}
		}
	};


	public static class WndAlchemizeItem extends WndInfoItem {

		private static final int BTN_HEIGHT	= 18;

		private WndBag owner;

		public WndAlchemizeItem(Item item, WndBag owner) {
			super(item);

			this.owner = owner;

			float pos = height;
			ArrayList<Component> buttons = new ArrayList<>();

			if (Shopkeeper.canSell(item)) {
				if (item.quantity() == 1) {

					RedButton btnSell = new RedButton(Messages.get(this, "sell", item.value())) {
						@Override
						protected void onClick() {
							WndTradeItem.sell(item);
							hide();
							consumeAlchemize();
						}
					};
					btnSell.setRect(0, pos + GAP, width, BTN_HEIGHT);
					btnSell.icon(new ItemSprite(ItemSpriteSheet.GOLD));
					buttons.add(btnSell);

					pos = btnSell.bottom();

				} else {

					int priceAll = item.value();
					RedButton btnSell1 = new RedButton(Messages.get(this, "sell_1", priceAll / item.quantity())) {
						@Override
						protected void onClick() {
							WndTradeItem.sellOne(item);
							hide();
							consumeAlchemize();
						}
					};
					btnSell1.setRect(0, pos + GAP, width, BTN_HEIGHT);
					btnSell1.icon(new ItemSprite(ItemSpriteSheet.GOLD));
					buttons.add(btnSell1);
					RedButton btnSellAll = new RedButton(Messages.get(this, "sell_all", priceAll)) {
						@Override
						protected void onClick() {
							WndTradeItem.sell(item);
							hide();
							consumeAlchemize();
						}
					};
					btnSellAll.setRect(0, btnSell1.bottom() + 1, width, BTN_HEIGHT);
					btnSellAll.icon(new ItemSprite(ItemSpriteSheet.GOLD));
					buttons.add(btnSellAll);

					pos = btnSellAll.bottom();

				}
			}

			if (item.energyVal() > 0) {
				if (item.quantity() == 1) {

					RedButton btnEnergize = new RedButton(Messages.get(this, "energize", item.energyVal())) {
						@Override
						protected void onClick() {
							WndEnergizeItem.energize(item);
							hide();
							consumeAlchemize();
						}
					};
					btnEnergize.setRect(0, pos + GAP, width, BTN_HEIGHT);
					btnEnergize.icon(new ItemSprite(ItemSpriteSheet.ENERGY));
					buttons.add(btnEnergize);

					pos = btnEnergize.bottom();

				} else {

					int energyAll = item.energyVal();
					RedButton btnEnergize1 = new RedButton(Messages.get(this, "energize_1", energyAll / item.quantity())) {
						@Override
						protected void onClick() {
							WndEnergizeItem.energizeOne(item);
							hide();
							consumeAlchemize();
						}
					};
					btnEnergize1.setRect(0, pos + GAP, width, BTN_HEIGHT);
					btnEnergize1.icon(new ItemSprite(ItemSpriteSheet.ENERGY));
					buttons.add(btnEnergize1);
					RedButton btnEnergizeAll = new RedButton(Messages.get(this, "energize_all", energyAll)) {
						@Override
						protected void onClick() {
							WndEnergizeItem.energize(item);
							hide();
							consumeAlchemize();
						}
					};
					btnEnergizeAll.setRect(0, btnEnergize1.bottom() + 1, width, BTN_HEIGHT);
					btnEnergizeAll.icon(new ItemSprite(ItemSpriteSheet.ENERGY));
					buttons.add(btnEnergizeAll);

					pos = btnEnergizeAll.bottom();

				}
			}

			addToBottom(buttons.toArray(new Component[0]));

		}

		private void consumeAlchemize(){
			Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
			if (curItem.quantity() <= 1){
				curItem.detachAll(Dungeon.hero.belongings.backpack);
				if (owner != null) {
					owner.hide();
				}
			} else {
				curItem.detach(Dungeon.hero.belongings.backpack);
				if (owner != null){
					owner.hide();
				}
				GameScene.selectItem(itemSelector);
			}
		}

	}
}
