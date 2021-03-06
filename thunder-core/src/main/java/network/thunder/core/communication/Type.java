/*
 * ThunderNetwork - Server Client Architecture to send Off-Chain Bitcoin Payments
 * Copyright (C) 2015 Mats Jerratsch <matsjj@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package network.thunder.core.communication;

/**
 * The Class Type.
 */
public class Type {

    public static int FAILURE = 000;

    public static int ESTABLISH_CHANNEL_A = 110;
    public static int ESTABLISH_CHANNEL_B = 111;
    public static int ESTABLISH_CHANNEL_C = 120;
    public static int ESTABLISH_CHANNEL_D = 121;

    public static int SEND_PAYMENT_ONE_REQUEST = 210;
    public static int SEND_PAYMENT_ONE_RESPONSE = 211;
    public static int SEND_PAYMENT_TWO_REQUEST = 220;
    public static int SEND_PAYMENT_TWO_RESPONSE = 221;

    public static int UPDATE_CHANNEL_ONE_REQUEST = 410;
    public static int UPDATE_CHANNEL_ONE_RESPONSE = 411;
    public static int UPDATE_CHANNEL_TWO_REQUEST = 420;
    public static int UPDATE_CHANNEL_TWO_RESPONSE = 421;

    public static int CLOSE_CHANNEL_REQUEST = 510;
    public static int CLOSE_CHANNEL_RESPONSE = 511;

    public static int WEBSOCKET_OPEN = 610;
    public static int WEBSOCKET_NEW_PAYMENT = 620;
    public static int WEBSOCKET_NEW_SECRET = 630;

    public static int AUTH_SEND = 1010;
    public static int AUTH_FAILED = 1012;

    public static int KEY_ENC_SEND = 1101;

    public static int GOSSIP_ADDR = 1201;
    public static int GOSSIP_INV = 1202;
    public static int GOSSIP_GET = 1203;
    public static int GOSSIP_SEND = 1204;
    public static int GOSSIP_GET_ADDR = 1211;
    public static int GOSSIP_GET_DATA_HEADER = 1212;
    public static int GOSSIP_SEND_IP_OBJECT = 1213; //IP objects are currently sent without an inv first..

    public static int SYNC_GET_IPS = 1301;
    public static int SYNC_SEND_IPS = 1302;
    public static int SYNC_GET_FRAGMENT = 1303;
    public static int SYNC_SEND_FRAGMENT = 1304;

}
